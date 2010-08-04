/*
 * Copyright (C) 2007 The Android Open Source Project
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

#include <cutils/zygote.h>

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <signal.h>

#ifndef NELEM
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

// pid of child process
static pid_t g_pid = -1;

static void signal_forwarder (int signal, siginfo_t *si, void *context)
{
    if (g_pid >= 0) {
        kill(g_pid, signal);
    }
}

static void post_run_func (int pid) {
    int my_pgid;
    int spawned_pgid;
    int i;
    int err;

    g_pid = pid;

    my_pgid = getpgid(0);
    if (my_pgid < 0) {
        perror ("error with getpgid()");
        exit (-1);
    }

    spawned_pgid = getpgid(pid);
    if (spawned_pgid < 0) {
        perror ("error with getpgid()");
        exit (-1);
    }

    if (my_pgid != spawned_pgid) {
        // The zygote was unable to move this process into our pgid
        // We have to forward signals

        int forward_signals[]
            = {SIGHUP, SIGINT, SIGTERM, SIGWINCH,
            SIGTSTP, SIGTTIN, SIGTTOU, SIGCONT};

        struct sigaction sa;
        memset(&sa, 0, sizeof(sa));

        sa.sa_sigaction = signal_forwarder;
        sa.sa_flags = SA_SIGINFO;

        for (i = 0; i < NELEM(forward_signals); i++) {
            err = sigaction(forward_signals[i], &sa, NULL);
            if (err < 0) {
                perror ("unexpected error");
                exit (-1);
            }
        }
    }
}

static void usage(const char *argv0) {
    fprintf(stderr,"Usage: %s [--help] [-classpath <classpath>] \n"
    "\t[additional zygote args] fully.qualified.java.ClassName [args]\n", argv0);
    fprintf(stderr, "\nRequests a new Dalvik VM instance to be spawned from the zygote\n"
    "process. stdin, stdout, and stderr are hooked up. This process remains\n"
    "while the spawned VM instance is alive and forwards some signals.\n"
    "The exit code of the spawned VM instance is dropped.\n");
}

int main (int argc, const char **argv) {
    int err;

    if (argc > 1 && 0 == strcmp(argv[1], "--help")) {
        usage(argv[0]);
        exit(0);
    }

    err = zygote_run_wait(argc - 1, argv + 1, post_run_func);

    if (err < 0) {
        fprintf(stderr, "%s error: no zygote process found\n", argv[0]);
        exit(-1);
    }
    exit(0);
}
