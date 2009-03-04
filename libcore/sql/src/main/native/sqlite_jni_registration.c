/*
 * Copyright 2007, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

#include "JNIHelp.h"
#include "sqlite_jni.h"

/* Methods for class SQLite_Database */
extern JNIEXPORT void JNICALL Java_SQLite_Database__1open
  (JNIEnv *, jobject, jstring, jint);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1open_1aux_1file
  (JNIEnv *, jobject, jstring);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1finalize
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1close
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1exec__Ljava_lang_String_2LSQLite_Callback_2
  (JNIEnv *, jobject, jstring, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1exec__Ljava_lang_String_2LSQLite_Callback_2_3Ljava_lang_String_2
  (JNIEnv *, jobject, jstring, jobject, jobjectArray);
extern JNIEXPORT jlong JNICALL Java_SQLite_Database__1last_1insert_1rowid
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1interrupt
  (JNIEnv *, jobject);
extern JNIEXPORT jlong JNICALL Java_SQLite_Database__1changes
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1busy_1handler
  (JNIEnv *, jobject, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1busy_1timeout
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jboolean JNICALL Java_SQLite_Database__1complete
  (JNIEnv *, jclass, jstring);
extern JNIEXPORT jstring JNICALL Java_SQLite_Database_version
  (JNIEnv *, jclass);
extern JNIEXPORT jstring JNICALL Java_SQLite_Database_dbversion
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1create_1function
  (JNIEnv *, jobject, jstring, jint, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1create_1aggregate
  (JNIEnv *, jobject, jstring, jint, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1function_1type
  (JNIEnv *, jobject, jstring, jint);
extern JNIEXPORT jstring JNICALL Java_SQLite_Database__1errmsg
  (JNIEnv *, jobject);
extern JNIEXPORT jstring JNICALL Java_SQLite_Database_error_1string
  (JNIEnv *, jclass, jint);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1set_1encoding
  (JNIEnv *, jobject, jstring);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1set_1authorizer
  (JNIEnv *, jobject, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1trace
  (JNIEnv *, jobject, jobject);
extern JNIEXPORT jboolean JNICALL Java_SQLite_Database_is3
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database_vm_1compile
  (JNIEnv *, jobject, jstring, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database_vm_1compile_1args
  (JNIEnv *, jobject, jstring, jobject, jobjectArray);
extern JNIEXPORT void JNICALL Java_SQLite_Database_stmt_1prepare
  (JNIEnv *, jobject, jstring, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1open_1blob
  (JNIEnv *, jobject, jstring, jstring, jstring, jlong, jboolean, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database__1progress_1handler
  (JNIEnv *, jobject, jint, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Database_internal_1init
  (JNIEnv *, jclass);


/* Methods for class SQLite_Vm */

extern JNIEXPORT jboolean JNICALL Java_SQLite_Vm_step
  (JNIEnv *, jobject, jobject);
extern JNIEXPORT jboolean JNICALL Java_SQLite_Vm_compile
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Vm_stop
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Vm_finalize
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Vm_internal_1init
  (JNIEnv *, jclass);

/* Methods for class SQLite_FunctionContext */

extern JNIEXPORT void JNICALL Java_SQLite_FunctionContext_set_1result__Ljava_lang_String_2
  (JNIEnv *, jobject, jstring);
extern JNIEXPORT void JNICALL Java_SQLite_FunctionContext_set_1result__I
  (JNIEnv *, jobject, jint);
extern JNIEXPORT void JNICALL Java_SQLite_FunctionContext_set_1result__D
  (JNIEnv *, jobject, jdouble);
extern JNIEXPORT void JNICALL Java_SQLite_FunctionContext_set_1error
  (JNIEnv *, jobject, jstring);
extern JNIEXPORT void JNICALL Java_SQLite_FunctionContext_set_1result___3B
  (JNIEnv *, jobject, jbyteArray);
extern JNIEXPORT void JNICALL Java_SQLite_FunctionContext_set_1result_1zeroblob
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jint JNICALL Java_SQLite_FunctionContext_count
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_FunctionContext_internal_1init
  (JNIEnv *, jclass);

/* Methods for class SQLite_Stmt */

extern JNIEXPORT jboolean JNICALL Java_SQLite_Stmt_prepare
  (JNIEnv *, jobject);
extern JNIEXPORT jboolean JNICALL Java_SQLite_Stmt_step
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_close
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_reset
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_clear_1bindings
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_bind__II
  (JNIEnv *, jobject, jint, jint);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_bind__IJ
  (JNIEnv *, jobject, jint, jlong);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_bind__ID
  (JNIEnv *, jobject, jint, jdouble);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_bind__I_3B
  (JNIEnv *, jobject, jint, jbyteArray);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_bind__ILjava_lang_String_2
  (JNIEnv *, jobject, jint, jstring);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_bind__I
  (JNIEnv *, jobject, jint);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_bind_1zeroblob
  (JNIEnv *, jobject, jint, jint);
extern JNIEXPORT jint JNICALL Java_SQLite_Stmt_bind_1parameter_1count
  (JNIEnv *, jobject);
extern JNIEXPORT jstring JNICALL Java_SQLite_Stmt_bind_1parameter_1name
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jint JNICALL Java_SQLite_Stmt_bind_1parameter_1index
  (JNIEnv *, jobject, jstring);
extern JNIEXPORT jint JNICALL Java_SQLite_Stmt_column_1int
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jlong JNICALL Java_SQLite_Stmt_column_1long
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jdouble JNICALL Java_SQLite_Stmt_column_1double
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jbyteArray JNICALL Java_SQLite_Stmt_column_1bytes
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jstring JNICALL Java_SQLite_Stmt_column_1string
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jint JNICALL Java_SQLite_Stmt_column_1type
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jint JNICALL Java_SQLite_Stmt_column_1count
  (JNIEnv *, jobject);
extern JNIEXPORT jstring JNICALL Java_SQLite_Stmt_column_1table_1name
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jstring JNICALL Java_SQLite_Stmt_column_1database_1name
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jstring JNICALL Java_SQLite_Stmt_column_1decltype
  (JNIEnv *, jobject, jint);
extern JNIEXPORT jstring JNICALL Java_SQLite_Stmt_column_1origin_1name
  (JNIEnv *, jobject, jint);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_finalize
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Stmt_internal_1init
  (JNIEnv *, jclass);

/* Methods for class SQLite_Blob */

extern JNIEXPORT void JNICALL Java_SQLite_Blob_close
  (JNIEnv *, jobject);
extern JNIEXPORT jint JNICALL Java_SQLite_Blob_write
  (JNIEnv *, jobject, jbyteArray, jint, jint, jint);
extern JNIEXPORT jint JNICALL Java_SQLite_Blob_read
  (JNIEnv *, jobject, jbyteArray, jint, jint, jint);
extern JNIEXPORT void JNICALL Java_SQLite_Blob_finalize
  (JNIEnv *, jobject);
extern JNIEXPORT void JNICALL Java_SQLite_Blob_internal_1init
  (JNIEnv *, jclass);

/*
 * JNI registration
 */
static JNINativeMethod sqliteDatabaseMethods[] = {
    /* name, signature, funcPtr */
/* Header for class SQLite_Database */
{ "_open", "(Ljava/lang/String;I)V", Java_SQLite_Database__1open},
{ "_open_aux_file", "(Ljava/lang/String;)V", Java_SQLite_Database__1open_1aux_1file},
{ "_finalize", "()V", Java_SQLite_Database__1finalize},
{ "_close", "()V", Java_SQLite_Database__1close},
{ "_exec", "(Ljava/lang/String;LSQLite/Callback;)V", Java_SQLite_Database__1exec__Ljava_lang_String_2LSQLite_Callback_2},
{ "_exec", "(Ljava/lang/String;LSQLite/Callback;[Ljava/lang/String;)V", Java_SQLite_Database__1exec__Ljava_lang_String_2LSQLite_Callback_2_3Ljava_lang_String_2},
{ "_last_insert_rowid", "()J", Java_SQLite_Database__1last_1insert_1rowid},
{ "_interrupt", "()V", Java_SQLite_Database__1interrupt},
{ "_changes", "()J", Java_SQLite_Database__1changes},
{ "_busy_handler", "(LSQLite/BusyHandler;)V", Java_SQLite_Database__1busy_1handler},
{ "_busy_timeout", "(I)V", Java_SQLite_Database__1busy_1timeout},
{ "_complete", "(Ljava/lang/String;)Z", Java_SQLite_Database__1complete},
{ "version", "()Ljava/lang/String;", Java_SQLite_Database_version},
{ "dbversion", "()Ljava/lang/String;", Java_SQLite_Database_dbversion},
{ "_create_function", "(Ljava/lang/String;ILSQLite/Function;)V", Java_SQLite_Database__1create_1function},
{ "_create_aggregate", "(Ljava/lang/String;ILSQLite/Function;)V", Java_SQLite_Database__1create_1aggregate},
{ "_function_type", "(Ljava/lang/String;I)V", Java_SQLite_Database__1function_1type},
{ "_errmsg", "()Ljava/lang/String;", Java_SQLite_Database__1errmsg},
{ "error_string", "(I)Ljava/lang/String;", Java_SQLite_Database_error_1string},
{ "_set_encoding", "(Ljava/lang/String;)V", Java_SQLite_Database__1set_1encoding},
{ "_set_authorizer", "(LSQLite/Authorizer;)V", Java_SQLite_Database__1set_1authorizer},
{ "_trace", "(LSQLite/Trace;)V", Java_SQLite_Database__1trace},
{ "is3", "()Z", Java_SQLite_Database_is3},
{ "vm_compile", "(Ljava/lang/String;LSQLite/Vm;)V", Java_SQLite_Database_vm_1compile},
{ "vm_compile_args", "(Ljava/lang/String;LSQLite/Vm;[Ljava/lang/String;)V", Java_SQLite_Database_vm_1compile_1args},
{ "stmt_prepare", "(Ljava/lang/String;LSQLite/Stmt;)V", Java_SQLite_Database_stmt_1prepare},
{ "_open_blob", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JZLSQLite/Blob;)V", Java_SQLite_Database__1open_1blob},
{ "_progress_handler", "(ILSQLite/ProgressHandler;)V", Java_SQLite_Database__1progress_1handler},
{ "internal_init", "()V", Java_SQLite_Database_internal_1init}
};

static JNINativeMethod sqliteVmMethods[] = {
/* Header for class SQLite_Vm */
{ "step", "(LSQLite/Callback;)Z", Java_SQLite_Vm_step},
{ "compile", "()Z", Java_SQLite_Vm_compile},
{ "stop", "()V", Java_SQLite_Vm_stop},
{ "finalize", "()V", Java_SQLite_Vm_finalize},
{ "internal_init", "()V", Java_SQLite_Vm_internal_1init}
};

static JNINativeMethod sqliteFunctionContextMethods[] = {
/* Header for class SQLite_FunctionContext */
{ "set_result", "(Ljava/lang/String;)V", Java_SQLite_FunctionContext_set_1result__Ljava_lang_String_2},
{ "set_result", "(I)V", Java_SQLite_FunctionContext_set_1result__I},
{ "set_result", "(D)V", Java_SQLite_FunctionContext_set_1result__D},
{ "set_error", "(Ljava/lang/String;)V", Java_SQLite_FunctionContext_set_1error},
{ "set_result", "([B)V", Java_SQLite_FunctionContext_set_1result___3B},
{ "set_result_zeroblob", "(I)V", Java_SQLite_FunctionContext_set_1result_1zeroblob},
{ "count", "()I", Java_SQLite_FunctionContext_count},
{ "internal_init", "()V", Java_SQLite_FunctionContext_internal_1init}
};

static JNINativeMethod sqliteStmtMethods[] = {
/* Header for class SQLite_Stmt */
{ "prepare", "()Z", Java_SQLite_Stmt_prepare},
{ "step", "()Z", JNICALL Java_SQLite_Stmt_step},
{ "close", "()V", Java_SQLite_Stmt_close},
{ "reset", "()V", Java_SQLite_Stmt_reset},
{ "clear_bindings", "()V", Java_SQLite_Stmt_clear_1bindings},
{ "bind", "(II)V", Java_SQLite_Stmt_bind__II},
{ "bind", "(IJ)V", Java_SQLite_Stmt_bind__IJ},
{ "bind", "(ID)V", Java_SQLite_Stmt_bind__ID},
{ "bind", "(I[B)V", Java_SQLite_Stmt_bind__I_3B},
{ "bind", "(ILjava/lang/String;)V", Java_SQLite_Stmt_bind__ILjava_lang_String_2},
{ "bind", "(I)V", Java_SQLite_Stmt_bind__I},
{ "bind_zeroblob", "(II)V", Java_SQLite_Stmt_bind_1zeroblob},
{ "bind_parameter_count", "()I", Java_SQLite_Stmt_bind_1parameter_1count},
{ "bind_parameter_name", "(I)Ljava/lang/String;", Java_SQLite_Stmt_bind_1parameter_1name},
{ "bind_parameter_index", "(Ljava/lang/String;)I", Java_SQLite_Stmt_bind_1parameter_1index},
{ "column_int", "(I)I", Java_SQLite_Stmt_column_1int},
{ "column_long", "(I)J", Java_SQLite_Stmt_column_1long},
{ "column_double", "(I)D", Java_SQLite_Stmt_column_1double},
{ "column_bytes", "(I)[B", Java_SQLite_Stmt_column_1bytes},
{ "column_string", "(I)Ljava/lang/String;", Java_SQLite_Stmt_column_1string},
{ "column_type", "(I)I", Java_SQLite_Stmt_column_1type},
{ "column_count", "()I", Java_SQLite_Stmt_column_1count},
{ "column_table_name", "(I)Ljava/lang/String;", Java_SQLite_Stmt_column_1table_1name},
{ "column_database_name", "(I)Ljava/lang/String;", Java_SQLite_Stmt_column_1database_1name},
{ "column_decltype", "(I)Ljava/lang/String;", Java_SQLite_Stmt_column_1decltype},
{ "column_origin_name", "(I)Ljava/lang/String;", Java_SQLite_Stmt_column_1origin_1name},
{ "finalize", "()V", Java_SQLite_Stmt_finalize},
{ "internal_init", "()V", Java_SQLite_Stmt_internal_1init}
};

static JNINativeMethod sqliteBlobMethods[] = {
/* Header for class SQLite_Blob */

{ "close", "()V", Java_SQLite_Blob_close},
{ "write", "([BIII)I", Java_SQLite_Blob_write},
{ "read", "([BIII)I", Java_SQLite_Blob_read},
{ "finalize", "()V", Java_SQLite_Blob_finalize},
{ "internal_init", "()V", Java_SQLite_Blob_internal_1init}
};

int register_SQLite_Database(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "SQLite/Database",
        sqliteDatabaseMethods, NELEM(sqliteDatabaseMethods));
}

int register_SQLite_Vm(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "SQLite/Vm",
        sqliteVmMethods, NELEM(sqliteVmMethods));
}

int register_SQLite_FunctionContext(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "SQLite/FunctionContext",
        sqliteFunctionContextMethods, NELEM(sqliteFunctionContextMethods));
}

int register_SQLite_Stmt(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "SQLite/Stmt",
        sqliteStmtMethods, NELEM(sqliteStmtMethods));
}

int register_SQLite_Blob(JNIEnv* env) {
    return jniRegisterNativeMethods(env, "SQLite/Blob",
        sqliteBlobMethods, NELEM(sqliteBlobMethods));
}
