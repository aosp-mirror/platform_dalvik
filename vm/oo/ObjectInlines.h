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
 * Helper functions to access data fields in Objects.
 */
#ifndef _DALVIK_OO_OBJECTACCESS
#define _DALVIK_OO_OBJECTACCESS

/*
 * Store a single value in the array, and note in the write barrier.
 */
INLINE void dvmSetObjectArrayElement(const ArrayObject* obj, int index,
                                     Object* val) {
    ((Object **)(obj)->contents)[index] = val;
    dvmWriteBarrierArray(obj, index, index+1);
}


/*
 * Field access functions.  Pass in the word offset from Field->byteOffset.
 *
 * We guarantee that long/double field data is 64-bit aligned, so it's safe
 * to access them with ldrd/strd on ARM.
 *
 * The VM treats all fields as 32 or 64 bits, so the field set functions
 * write 32 bits even if the underlying type is smaller.
 */
#define BYTE_OFFSET(_ptr, _offset)  ((void*) (((u1*)(_ptr)) + (_offset)))

INLINE JValue* dvmFieldPtr(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset));
}

INLINE bool dvmGetFieldBoolean(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset))->z;
}
INLINE s1 dvmGetFieldByte(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset))->b;
}
INLINE s2 dvmGetFieldShort(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset))->s;
}
INLINE u2 dvmGetFieldChar(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset))->c;
}
INLINE s4 dvmGetFieldInt(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset))->i;
}
INLINE s8 dvmGetFieldLong(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset))->j;
}
INLINE float dvmGetFieldFloat(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset))->f;
}
INLINE double dvmGetFieldDouble(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset))->d;
}
INLINE Object* dvmGetFieldObject(const Object* obj, int offset) {
    return ((JValue*)BYTE_OFFSET(obj, offset))->l;
}
INLINE s4 dvmGetFieldIntVolatile(const Object* obj, int offset) {
    s4* ptr = &((JValue*)BYTE_OFFSET(obj, offset))->i;
    return android_atomic_acquire_load(ptr);
}
INLINE Object* dvmGetFieldObjectVolatile(const Object* obj, int offset) {
    void** ptr = &((JValue*)BYTE_OFFSET(obj, offset))->l;
    return (Object*) android_atomic_acquire_load((int32_t*)ptr);
}
INLINE s8 dvmGetFieldLongVolatile(const Object* obj, int offset) {
    const s8* addr = BYTE_OFFSET(obj, offset);
    s8 val = dvmQuasiAtomicRead64(addr);
    ANDROID_MEMBAR_FULL();
    return val;
}

INLINE void dvmSetFieldBoolean(Object* obj, int offset, bool val) {
    ((JValue*)BYTE_OFFSET(obj, offset))->i = val;
}
INLINE void dvmSetFieldByte(Object* obj, int offset, s1 val) {
    ((JValue*)BYTE_OFFSET(obj, offset))->i = val;
}
INLINE void dvmSetFieldShort(Object* obj, int offset, s2 val) {
    ((JValue*)BYTE_OFFSET(obj, offset))->i = val;
}
INLINE void dvmSetFieldChar(Object* obj, int offset, u2 val) {
    ((JValue*)BYTE_OFFSET(obj, offset))->i = val;
}
INLINE void dvmSetFieldInt(Object* obj, int offset, s4 val) {
    ((JValue*)BYTE_OFFSET(obj, offset))->i = val;
}
INLINE void dvmSetFieldLong(Object* obj, int offset, s8 val) {
    ((JValue*)BYTE_OFFSET(obj, offset))->j = val;
}
INLINE void dvmSetFieldFloat(Object* obj, int offset, float val) {
    ((JValue*)BYTE_OFFSET(obj, offset))->f = val;
}
INLINE void dvmSetFieldDouble(Object* obj, int offset, double val) {
    ((JValue*)BYTE_OFFSET(obj, offset))->d = val;
}
INLINE void dvmSetFieldObject(Object* obj, int offset, Object* val) {
    JValue* lhs = BYTE_OFFSET(obj, offset);
    lhs->l = val;
    dvmWriteBarrierField(obj, &lhs->l);
}
INLINE void dvmSetFieldIntVolatile(Object* obj, int offset, s4 val) {
    s4* ptr = &((JValue*)BYTE_OFFSET(obj, offset))->i;
    android_atomic_release_store(val, ptr);
}
INLINE void dvmSetFieldObjectVolatile(Object* obj, int offset, Object* val) {
    void** ptr = &((JValue*)BYTE_OFFSET(obj, offset))->l;
    android_atomic_release_store((int32_t)val, (int32_t*)ptr);
    dvmWriteBarrierField(obj, ptr);
}
INLINE void dvmSetFieldLongVolatile(Object* obj, int offset, s8 val) {
    s8* addr = BYTE_OFFSET(obj, offset);
    ANDROID_MEMBAR_FULL();
    dvmQuasiAtomicSwap64(val, addr);
}

/*
 * Static field access functions.
 */
INLINE JValue* dvmStaticFieldPtr(const StaticField* sfield) {
    return (JValue*)&sfield->value;
}

INLINE bool dvmGetStaticFieldBoolean(const StaticField* sfield) {
    return sfield->value.z;
}
INLINE s1 dvmGetStaticFieldByte(const StaticField* sfield) {
    return sfield->value.b;
}
INLINE s2 dvmGetStaticFieldShort(const StaticField* sfield) {
    return sfield->value.s;
}
INLINE u2 dvmGetStaticFieldChar(const StaticField* sfield) {
    return sfield->value.c;
}
INLINE s4 dvmGetStaticFieldInt(const StaticField* sfield) {
    return sfield->value.i;
}
INLINE s8 dvmGetStaticFieldLong(const StaticField* sfield) {
    return sfield->value.j;
}
INLINE float dvmGetStaticFieldFloat(const StaticField* sfield) {
    return sfield->value.f;
}
INLINE double dvmGetStaticFieldDouble(const StaticField* sfield) {
    return sfield->value.d;
}
INLINE Object* dvmGetStaticFieldObject(const StaticField* sfield) {
    return sfield->value.l;
}
INLINE s4 dvmGetStaticFieldIntVolatile(const StaticField* sfield) {
    const s4* ptr = &(sfield->value.i);
    return android_atomic_acquire_load((s4*)ptr);
}
INLINE Object* dvmGetStaticFieldObjectVolatile(const StaticField* sfield) {
    void* const* ptr = &(sfield->value.l);
    return (Object*) android_atomic_acquire_load((int32_t*)ptr);
}
INLINE s8 dvmGetStaticFieldLongVolatile(const StaticField* sfield) {
    const s8* addr = &sfield->value.j;
    s8 val = dvmQuasiAtomicRead64(addr);
    ANDROID_MEMBAR_FULL();
    return val;
}

INLINE void dvmSetStaticFieldBoolean(StaticField* sfield, bool val) {
    sfield->value.i = val;
}
INLINE void dvmSetStaticFieldByte(StaticField* sfield, s1 val) {
    sfield->value.i = val;
}
INLINE void dvmSetStaticFieldShort(StaticField* sfield, s2 val) {
    sfield->value.i = val;
}
INLINE void dvmSetStaticFieldChar(StaticField* sfield, u2 val) {
    sfield->value.i = val;
}
INLINE void dvmSetStaticFieldInt(StaticField* sfield, s4 val) {
    sfield->value.i = val;
}
INLINE void dvmSetStaticFieldLong(StaticField* sfield, s8 val) {
    sfield->value.j = val;
}
INLINE void dvmSetStaticFieldFloat(StaticField* sfield, float val) {
    sfield->value.f = val;
}
INLINE void dvmSetStaticFieldDouble(StaticField* sfield, double val) {
    sfield->value.d = val;
}
INLINE void dvmSetStaticFieldObject(StaticField* sfield, Object* val) {
    sfield->value.l = val;
    dvmWriteBarrierField((Object *)sfield->field.clazz, &sfield->value.l);
}
INLINE void dvmSetStaticFieldIntVolatile(StaticField* sfield, s4 val) {
    s4* ptr = &(sfield->value.i);
    android_atomic_release_store(val, ptr);
}
INLINE void dvmSetStaticFieldObjectVolatile(StaticField* sfield, Object* val) {
    void** ptr = &(sfield->value.l);
    android_atomic_release_store((int32_t)val, (int32_t*)ptr);
    dvmWriteBarrierField((Object *)sfield->field.clazz, &sfield->value.l);
}
INLINE void dvmSetStaticFieldLongVolatile(StaticField* sfield, s8 val) {
    s8* addr = &sfield->value.j;
    ANDROID_MEMBAR_FULL();
    dvmQuasiAtomicSwap64(val, addr);
}

#endif /*_DALVIK_OO_OBJECTACCESS*/
