/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Implementation of java.lang.reflect.Proxy.
 *
 * Traditionally this is implemented entirely in interpreted code,
 * generating bytecode that defines the proxy class.  Dalvik doesn't
 * currently support this approach, so we generate the class directly.  If
 * we add support for DefineClass with standard classfiles we can
 * eliminate this.
 */
#include "Dalvik.h"

#include <stdlib.h>

// fwd
static bool gatherMethods(ArrayObject* interfaces, Method*** pMethods,
    int* pMethodCount);
static bool addMethod(Method* meth, Method** methArray, int slot);
static void createConstructor(ClassObject* clazz, Method* meth);
static void createHandlerMethod(ClassObject* clazz, Method* dstMeth,
    const Method* srcMeth);
static void proxyConstructor(const u4* args, JValue* pResult,
    const Method* method, Thread* self);
static void proxyInvoker(const u4* args, JValue* pResult,
    const Method* method, Thread* self);

/*
 * Perform Proxy setup.
 */
bool dvmReflectProxyStartup()
{
    /*
     * Standard methods we must provide in our proxy.
     */
    Method* methE;
    Method* methH;
    Method* methT;
    Method* methF;
    methE = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangObject, "equals",
            "(Ljava/lang/Object;)Z");
    methH = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangObject, "hashCode",
            "()I");
    methT = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangObject, "toString",
            "()Ljava/lang/String;");
    methF = dvmFindVirtualMethodByDescriptor(gDvm.classJavaLangObject, "finalize",
            "()V");
    if (methE == NULL || methH == NULL || methT == NULL || methF == NULL) {
        LOGE("Could not find equals/hashCode/toString/finalize in Object\n");
        return false;
    }
    gDvm.voffJavaLangObject_equals = methE->methodIndex;
    gDvm.voffJavaLangObject_hashCode = methH->methodIndex;
    gDvm.voffJavaLangObject_toString = methT->methodIndex;
    gDvm.voffJavaLangObject_finalize = methF->methodIndex;

    /*
     * The prototype signature needs to be cloned from a method in a
     * "real" DEX file.  We declared this otherwise unused method just
     * for this purpose.
     */
    ClassObject* proxyClass;
    Method* meth;
    proxyClass = dvmFindSystemClassNoInit("Ljava/lang/reflect/Proxy;");
    if (proxyClass == NULL) {
        LOGE("No java.lang.reflect.Proxy\n");
        return false;
    }
    meth = dvmFindDirectMethodByDescriptor(proxyClass, "constructorPrototype",
                "(Ljava/lang/reflect/InvocationHandler;)V");
    if (meth == NULL) {
        LOGE("Could not find java.lang.Proxy.constructorPrototype()\n");
        return false;
    }
    gDvm.methJavaLangReflectProxy_constructorPrototype = meth;

    return true;
}


/*
 * Generate a proxy class with the specified name, interfaces, and loader.
 * "interfaces" is an array of class objects.
 *
 * The interpreted code has done all of the necessary checks, e.g. we know
 * that "interfaces" contains only interface classes.
 *
 * On failure we leave a partially-created class object sitting around,
 * but the garbage collector will take care of it.
 */
ClassObject* dvmGenerateProxyClass(StringObject* str, ArrayObject* interfaces,
    Object* loader)
{
    int result = -1;
    char* nameStr = NULL;
    Method** methods = NULL;
    ClassObject* newClass = NULL;
    int i;
    
    nameStr = dvmCreateCstrFromString(str);
    if (nameStr == NULL) {
        dvmThrowException("Ljava/lang/IllegalArgumentException;",
            "missing name");
        goto bail;
    }

    LOGV("+++ Generate proxy class '%s' %p from %d interface classes\n",
        nameStr, loader, interfaces->length);


    /*
     * Characteristics of a Proxy class:
     * - concrete class, public and final
     * - superclass is java.lang.reflect.Proxy
     * - implements all listed interfaces (req'd for instanceof)
     * - has one method for each method in the interfaces (barring duplicates)
     * - has one constructor (takes an InvocationHandler arg)
     * - has overrides for hashCode, equals, and toString (these come first)
     * - has one field, a reference to the InvocationHandler object
     *
     * The idea here is to create a class object and fill in the details
     * as we would in loadClassFromDex(), and then call dvmLinkClass() to do
     * all the heavy lifting (notably populating the virtual and interface
     * method tables).
     */

    /*
     * Generate a temporary list of virtual methods.
     */
    int methodCount;
    if (!gatherMethods(interfaces, &methods, &methodCount))
        goto bail;

    /*
     * Allocate storage for the class object and set some basic fields.
     */
    newClass = (ClassObject*) dvmMalloc(sizeof(*newClass), ALLOC_DEFAULT);
    if (newClass == NULL)
        return NULL;
    DVM_OBJECT_INIT(&newClass->obj, gDvm.unlinkedJavaLangClass);
    newClass->descriptorAlloc = dvmNameToDescriptor(nameStr);
    newClass->descriptor = newClass->descriptorAlloc;
    newClass->accessFlags = ACC_PUBLIC | ACC_FINAL;
    newClass->super = gDvm.classJavaLangReflectProxy;
    newClass->primitiveType = PRIM_NOT;
    newClass->classLoader = loader;
#if WITH_HPROF && WITH_HPROF_STACK
    newClass->hprofSerialNumber = 0;
    hprofFillInStackTrace(newClass);
#endif

    /*
     * Add direct method definitions.  We have one (the constructor).
     */
    newClass->directMethodCount = 1;
    newClass->directMethods = (Method*) dvmLinearAlloc(newClass->classLoader,
            1 * sizeof(Method));
    createConstructor(newClass, &newClass->directMethods[0]);
    dvmLinearReadOnly(newClass->classLoader, newClass->directMethods);

    /*
     * Add virtual method definitions.
     */
    newClass->virtualMethodCount = methodCount;
    newClass->virtualMethods = (Method*) dvmLinearAlloc(newClass->classLoader,
            newClass->virtualMethodCount * sizeof(Method));
    for (i = 0; i < newClass->virtualMethodCount; i++) {
        createHandlerMethod(newClass, &newClass->virtualMethods[i],methods[i]);
    }
    dvmLinearReadOnly(newClass->classLoader, newClass->virtualMethods);

    /*
     * Add interface list.
     */
    int interfaceCount = interfaces->length;
    ClassObject** ifArray = (ClassObject**) interfaces->contents;
    newClass->interfaceCount = interfaceCount;
    newClass->interfaces = (ClassObject**)dvmLinearAlloc(newClass->classLoader,
                                sizeof(ClassObject*) * interfaceCount);
    for (i = 0; i < interfaceCount; i++)
        newClass->interfaces[i] = ifArray[i];
    dvmLinearReadOnly(newClass->classLoader, newClass->interfaces);

    /*
     * The class has one instance field, "protected InvocationHandler h",
     * which is filled in by the constructor.
     */
    newClass->ifieldCount = 1;
    newClass->ifields = (InstField*) dvmLinearAlloc(newClass->classLoader,
            1 * sizeof(InstField));
    InstField* ifield = &newClass->ifields[0];
    ifield->field.clazz = newClass;
    ifield->field.name = "h";
    ifield->field.signature = "Ljava/lang/reflect/InvocationHandler;";
    ifield->field.accessFlags = ACC_PROTECTED;
    ifield->byteOffset = -1;        /* set later */
    dvmLinearReadOnly(newClass->classLoader, newClass->ifields);


    /*
     * Everything is ready.  See if the linker will lap it up.
     */
    newClass->status = CLASS_LOADED;
    if (!dvmLinkClass(newClass, true)) {
        LOGI("Proxy class link failed\n");
        goto bail;
    }

    /*
     * All good.  Add it to the hash table.  We should NOT see a collision
     * here; if we do, it means the caller has screwed up and provided us
     * with a duplicate name.
     */
    if (!dvmAddClassToHash(newClass)) {
        LOGE("ERROR: attempted to generate %s more than once\n",
            newClass->descriptor);
        goto bail;
    }

    result = 0;

bail:
    free(nameStr);
    free(methods);
    if (result != 0) {
        /* must free innards explicitly if we didn't finish linking */
        dvmFreeClassInnards(newClass);
        newClass = NULL;
        dvmThrowException("Ljava/lang/RuntimeException;", NULL);
    }

    /* this allows the GC to free it */
    dvmReleaseTrackedAlloc((Object*) newClass, NULL);

    return newClass;
}

/*
 * Generate a list of methods.  The Method pointers returned point to the
 * abstract method definition from the appropriate interface, or to the
 * virtual method definition in java.lang.Object.
 */
static bool gatherMethods(ArrayObject* interfaces, Method*** pMethods,
    int* pMethodCount)
{
    ClassObject** classes;
    Method** methods;
    int numInterfaces, maxCount, actualCount;
    int i;

    /*
     * Get a maximum count so we can allocate storage.  We need the
     * methods declared by each interface and all of its superinterfaces.
     */
    maxCount = 3;       // 3 methods in java.lang.Object
    numInterfaces = interfaces->length;
    classes = (ClassObject**) interfaces->contents;

    for (i = 0; i < numInterfaces; i++, classes++) {
        ClassObject* clazz = *classes;

        LOGVV("---  %s virtualMethodCount=%d\n",
            clazz->descriptor, clazz->virtualMethodCount);
        maxCount += clazz->virtualMethodCount;

        int j;
        for (j = 0; j < clazz->iftableCount; j++) {
            ClassObject* iclass = clazz->iftable[j].clazz;

            LOGVV("---  +%s %d\n",
                iclass->descriptor, iclass->virtualMethodCount);
            maxCount += iclass->virtualMethodCount;
        }
    }

    methods = (Method**) malloc(maxCount * sizeof(*methods));
    if (methods == NULL)
        return false;

    /*
     * First three entries are the java.lang.Object methods.
     */
    ClassObject* obj = gDvm.classJavaLangObject;
    methods[0] = obj->vtable[gDvm.voffJavaLangObject_equals];
    methods[1] = obj->vtable[gDvm.voffJavaLangObject_hashCode];
    methods[2] = obj->vtable[gDvm.voffJavaLangObject_toString];
    actualCount = 3;

    /*
     * Add the methods from each interface, in order, checking for
     * duplicates.  This is O(n^2), but that should be okay here.
     */
    classes = (ClassObject**) interfaces->contents;
    for (i = 0; i < numInterfaces; i++, classes++) {
        ClassObject* clazz = *classes;
        int j;

        for (j = 0; j < clazz->virtualMethodCount; j++) {
            if (addMethod(&clazz->virtualMethods[j], methods, actualCount))
                actualCount++;
        }

        for (j = 0; j < clazz->iftableCount; j++) {
            ClassObject* iclass = clazz->iftable[j].clazz;
            int k;

            for (k = 0; k < iclass->virtualMethodCount; k++) {
                if (addMethod(&iclass->virtualMethods[k], methods, actualCount))
                    actualCount++;
            }
        }
    }

    //for (i = 0; i < actualCount; i++) {
    //    LOGI(" %d: %s.%s\n",
    //        i, methods[i]->clazz->descriptor, methods[i]->name);
    //}

    *pMethods = methods;
    *pMethodCount = actualCount;
    return true;
}

/*
 * Add a method to "methArray" if a matching method does not already
 * exist.  Two methods match if they have the same name and signature.
 *
 * Returns "true" if the item was added, "false" if a duplicate was
 * found and the method was not added.
 */
static bool addMethod(Method* meth, Method** methArray, int slot)
{
    int i;

    for (i = 0; i < slot; i++) {
        if (dvmCompareMethodNamesAndProtos(methArray[i], meth) == 0) {
            return false;
        }
    }

    methArray[slot] = meth;
    return true;
}

/*
 * Create a constructor for our Proxy class.  The constructor takes one
 * argument, a java.lang.reflect.InvocationHandler.
 */
static void createConstructor(ClassObject* clazz, Method* meth)
{
    meth->clazz = clazz;
    meth->accessFlags = ACC_PUBLIC | ACC_NATIVE;
    meth->name = "<init>";
    meth->prototype =
        gDvm.methJavaLangReflectProxy_constructorPrototype->prototype;
    meth->shorty = 
        gDvm.methJavaLangReflectProxy_constructorPrototype->shorty;
    // no pDexCode or pDexMethod

    int argsSize = dvmComputeMethodArgsSize(meth) + 1;
    meth->registersSize = meth->insSize = argsSize;

    meth->nativeFunc = proxyConstructor;
}

/*
 * Create a method in our Proxy class with the name and signature of
 * the interface method it implements.
 */
static void createHandlerMethod(ClassObject* clazz, Method* dstMeth,
    const Method* srcMeth)
{
    dstMeth->clazz = clazz;
    dstMeth->insns = (u2*) srcMeth;
    dstMeth->accessFlags = ACC_PUBLIC | ACC_NATIVE;
    dstMeth->name = srcMeth->name;
    dstMeth->prototype = srcMeth->prototype;
    dstMeth->shorty = srcMeth->shorty;
    // no pDexCode or pDexMethod

    int argsSize = dvmComputeMethodArgsSize(dstMeth) + 1;
    dstMeth->registersSize = dstMeth->insSize = argsSize;

    dstMeth->nativeFunc = proxyInvoker;
}

/*
 * Return a new Object[] array with the contents of "args".  We determine
 * the number and types of values in "args" based on the method signature.
 * Primitive types are boxed.
 *
 * Returns NULL if the method takes no arguments.
 *
 * The caller must call dvmReleaseTrackedAlloc() on the return value.
 *
 * On failure, returns with an appropriate exception raised.
 */
static ArrayObject* boxMethodArgs(const Method* method, const u4* args)
{
    const char* desc = &method->shorty[1]; // [0] is the return type.
    ArrayObject* argArray = NULL;
    int argCount;
    Object** argObjects;
    bool failed = true;

    /* count args */
    argCount = dexProtoGetParameterCount(&method->prototype);

    /* allocate storage */
    argArray = dvmAllocArray(gDvm.classJavaLangObjectArray, argCount,
        kObjectArrayRefWidth, ALLOC_DEFAULT);
    if (argArray == NULL)
        goto bail;
    argObjects = (Object**) argArray->contents;

    /*
     * Fill in the array.
     */

    int srcIndex = 0;
    
    argCount = 0;
    while (*desc != '\0') {
        char descChar = *(desc++);
        JValue value;

        switch (descChar) {
        case 'Z':
        case 'C':
        case 'F':
        case 'B':
        case 'S':
        case 'I':
            value.i = args[srcIndex++];
            argObjects[argCount] = (Object*) dvmWrapPrimitive(value,
                dvmFindPrimitiveClass(descChar));
            /* argObjects is tracked, don't need to hold this too */
            dvmReleaseTrackedAlloc(argObjects[argCount], NULL);
            argCount++;
            break;
        case 'D':
        case 'J':
            value.j = dvmGetArgLong(args, srcIndex);
            srcIndex += 2;
            argObjects[argCount] = (Object*) dvmWrapPrimitive(value,
                dvmFindPrimitiveClass(descChar));
            dvmReleaseTrackedAlloc(argObjects[argCount], NULL);
            argCount++;
            break;
        case '[':
        case 'L':
            argObjects[argCount++] = (Object*) args[srcIndex++];
            break;
        }
    }

    failed = false;

bail:
    if (failed) {
        dvmReleaseTrackedAlloc((Object*)argArray, NULL);
        argArray = NULL;
    }
    return argArray;
}

/*
 * This is the constructor for a generated proxy object.
 */
static void proxyConstructor(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    Object* obj = (Object*) args[0];
    Object* handler = (Object*) args[1];
    ClassObject* clazz = obj->clazz;
    int fieldOffset;

    fieldOffset = dvmFindFieldOffset(clazz, "h",
                    "Ljava/lang/reflect/InvocationHandler;");
    if (fieldOffset < 0) {
        LOGE("Unable to find 'h' in Proxy object\n");
        //dvmDumpClass(clazz, kDumpClassFullDetail);
        dvmAbort();     // this should never happen
    }
    dvmSetFieldObject(obj, fieldOffset, handler);
}

/*
 * This is the common message body for proxy methods.
 *
 * The method we're calling looks like:
 *   public Object invoke(Object proxy, Method method, Object[] args)
 *
 * This means we have to create a Method object, box our arguments into
 * a new Object[] array, make the call, and unbox the return value if
 * necessary.
 */
static void proxyInvoker(const u4* args, JValue* pResult,
    const Method* method, Thread* self)
{
    Object* thisObj = (Object*) args[0];
    Object* methodObj = NULL;
    ArrayObject* argArray = NULL;
    Object* handler;
    Method* invoke;
    ClassObject* returnType;
    int hOffset;
    JValue invokeResult;

    /*
     * Retrieve handler object for this proxy instance.
     */
    hOffset = dvmFindFieldOffset(thisObj->clazz, "h",
                    "Ljava/lang/reflect/InvocationHandler;");
    if (hOffset < 0) {
        LOGE("Unable to find 'h' in Proxy object\n");
        dvmAbort();
    }
    handler = dvmGetFieldObject(thisObj, hOffset);

    /*
     * Find the invoke() method, looking in "this"s class.  (Because we
     * start here we don't have to convert it to a vtable index and then
     * index into this' vtable.)
     */
    invoke = dvmFindVirtualMethodHierByDescriptor(handler->clazz, "invoke",
            "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
    if (invoke == NULL) {
        LOGE("Unable to find invoke()\n");
        dvmAbort();
    }

    LOGV("invoke: %s.%s, this=%p, handler=%s\n",
        method->clazz->descriptor, method->name,
        thisObj, handler->clazz->descriptor);

    /*
     * Create a java.lang.reflect.Method object for this method.
     *
     * We don't want to use "method", because that's the concrete
     * implementation in the proxy class.  We want the abstract Method
     * from the declaring interface.  We have a pointer to it tucked
     * away in the "insns" field.
     *
     * TODO: this could be cached for performance.
     */
    methodObj = dvmCreateReflectMethodObject((Method*) method->insns);
    if (methodObj == NULL) {
        assert(dvmCheckException(self));
        goto bail;
    }

    /*
     * Determine the return type from the signature.
     *
     * TODO: this could be cached for performance.
     */
    returnType = dvmGetBoxedReturnType(method);
    if (returnType == NULL) {
        char* desc = dexProtoCopyMethodDescriptor(&method->prototype);
        LOGE("Could not determine return type for '%s'\n", desc);
        free(desc);
        assert(dvmCheckException(self));
        goto bail;
    }
    LOGV("  return type will be %s\n", returnType->descriptor);

    /*
     * Convert "args" array into Object[] array, using the method
     * signature to determine types.  If the method takes no arguments,
     * we must pass null.
     */
    argArray = boxMethodArgs(method, args+1);
    if (dvmCheckException(self))
        goto bail;

    /*
     * Call h.invoke(proxy, method, args).
     *
     * We don't need to repackage exceptions, so if one has been thrown
     * just jump to the end.
     */
    dvmCallMethod(self, invoke, handler, &invokeResult,
        thisObj, methodObj, argArray);
    if (dvmCheckException(self))
        goto bail;

    /*
     * Unbox the return value.  If it's the wrong type, throw a
     * ClassCastException.  If it's a null pointer and we need a
     * primitive type, throw a NullPointerException.
     */
    if (returnType->primitiveType == PRIM_VOID) {
        LOGVV("+++ ignoring return to void\n");
    } else if (invokeResult.l == NULL) {
        if (dvmIsPrimitiveClass(returnType)) {
            dvmThrowException("Ljava/lang/NullPointerException;",
                "null result when primitive expected");
            goto bail;
        }
        pResult->l = NULL;
    } else {
        if (!dvmUnwrapPrimitive(invokeResult.l, returnType, pResult)) {
            dvmThrowExceptionWithClassMessage("Ljava/lang/ClassCastException;",
                ((Object*)invokeResult.l)->clazz->descriptor);
            goto bail;
        }
    }

bail:
    dvmReleaseTrackedAlloc(methodObj, self);
    dvmReleaseTrackedAlloc((Object*)argArray, self);
}

