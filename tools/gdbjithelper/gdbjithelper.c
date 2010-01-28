/*
 * Copyright (C) 2010 The Android Open Source Project
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

#include <unistd.h>
#include <stdio.h>

volatile int done;

/*
 * See README.txt for detailed steps.
 *
 * If you see a native crash in the bugreport and the PC/LR are
 * pointing to the code cache address range, copy them into the following
 * arrays.
 *
 *        #00  pc 463ba204
 *        #01  lr 463ba1c9  <unknown>
 *
 * code around pc:
 * 463ba1e4 4300e119 4284aa7a f927f7b7 40112268
 * 463ba1f4 419da7f8 00002000 01000100 00080000
 * 463ba204 4191debc 01010000 4284aa74 68b00054
 * 463ba214 045cf205 cc016468 0718f2a5 d0102800
 * 463ba224 4c13c701 a20aa108 efb0f775 e008e010
 * 
 * code around lr:
 * 463ba1a8 42e19e58 f2050050 cc01045c 0718f2a5
 * 463ba1b8 d00f2800 4c13c701 a20aa108 efe4f775
 * 463ba1c8 e007e010 29006bf8 6e77dc01 a10347b8
 * 463ba1d8 ef60f775 6db1480b 1c2d4788 4300e119
 * 463ba1e8 4284aa7a f927f7b7 40112268 419da7f8
 *
 */

int codePC[] = {
    // Sample content
    0x4300e119, 0x4284aa7a, 0xf927f7b7, 0x40112268,
    0x419da7f8, 0x00002000, 0x01000100, 0x00080000,
    0x4191debc, 0x01010000, 0x4284aa74, 0x68b00054,
    0x045cf205, 0xcc016468, 0x0718f2a5, 0xd0102800,
    0x4c13c701, 0xa20aa108, 0xefb0f775, 0xe008e010,
};

int codeLR[] = {
    // Sample content
    0x42e19e58, 0xf2050050, 0xcc01045c, 0x0718f2a5,
    0xd00f2800, 0x4c13c701, 0xa20aa108, 0xefe4f775,
    0xe007e010, 0x29006bf8, 0x6e77dc01, 0xa10347b8,
    0xef60f775, 0x6db1480b, 0x1c2d4788, 0x4300e119,
    0x4284aa7a, 0xf927f7b7, 0x40112268, 0x419da7f8,
};

void dumpCode()
{
    unsigned int i;

    for (i = 0; i < sizeof(codePC)/sizeof(int); i++) {
        printf("codePC[%d]: %#x\n", i, codePC[i]);
    }

    for (i = 0; i < sizeof(codeLR)/sizeof(int); i++) {
        printf("codeLR[%d]: %#x\n", i, codeLR[i]);
    }
}

int main()
{
    dumpCode();
    while (!done) {
        sleep(1000);
    }
    return 0;
}
