#!/bin/sh
exec "$JAVACMD" "$@"

    )" '"$@"'
        tr '\n' ' '
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        xargs -n1 |
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
eval "set -- $(

#
# an unmatched quote.
# This will of course break if any of these variables contains a newline or
#
# the whole thing up as a single "set" statement.
# that process (while maintaining the separation between arguments), and wrap
# character that might be a shell metacharacter, then use eval to reverse
# post-process each arg (as a line of input to sed) to backslash-escape any
# but POSIX shell has neither arrays nor command substitution, so instead we
#
#   set -- "${ARGS[@]}" "$@"
#   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
#
# In Bash we could simply go:
#
# With -n1 it outputs one arg per line, with the quotes and backslashes removed.
#
# Use "xargs" to parse quoted args.

fi
    die "xargs is not available"
then
if ! command -v xargs >/dev/null 2>&1
# Stop when "xargs" is not available.

        "$@"
        org.gradle.wrapper.GradleWrapperMain \
        -classpath "$CLASSPATH" \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
set -- \

#     treated as '${Hostname}' itself on the command line.
#   * For example: A user cannot expect ${Hostname} to be expanded, as it is an environment variable and will be
#     and any embedded shellness will be escaped.
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, JAVA_OPTS, and optsEnvironmentVar are not allowed to contain shell fragments,
# Collect all arguments for the java command:

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.


fi
    done
        set -- "$@" "$arg"      # push replacement arg
        shift                   # remove old arg
        # iterations, nor the values presented in `arg`.
        # changing the positional parameters here affects neither the number of
        # NB: a `for` loop captures its iteration list before it begins, so
        #
        # possibly modified.
        # args, so each arg winds up back in the position where it started, but
        # Roll the args list around exactly as many times as the number of
        fi
            arg=$( cygpath --path --ignore --mixed "$arg" )
        then
            esac
              *)    false ;;
                    [ -e "$t" ] ;;                      #(
              /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
              -*)   false ;;                            # don't mess with options #(
            case $arg in                                #(
        if
    for arg do
    # Now convert the arguments - kludge to limit ourselves to /bin/sh

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
if "$cygwin" || "$msys" ; then
# For Cygwin or MSYS, switch paths to Windows format before running java

#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.
#   * --module-path (only if needed)
#   * -D...appname settings
#   * -classpath
#   * the main class name
#   * args from the command line
# Collect all arguments for the java command, stacking in reverse order:

fi
    esac
            warn "Could not set maximum file descriptor limit to $MAX_FD"
        ulimit -n "$MAX_FD" ||
        # shellcheck disable=SC3045
        # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
      *)
      '' | soft) :;; #(
    case $MAX_FD in  #(
    esac
            warn "Could not query maximum file descriptor limit"
        MAX_FD=$( ulimit -H -n ) ||
        # shellcheck disable=SC3045
        # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
      max*)
    case $MAX_FD in #(
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
# Increase the maximum file descriptors if we can.

fi
    fi
location of your Java installation."
Please set the JAVA_HOME variable in your environment to match the

        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    then
    if ! command -v java >/dev/null 2>&1
    JAVACMD=java
else
    fi
location of your Java installation."
Please set the JAVA_HOME variable in your environment to match the

        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME
    if [ ! -x "$JAVACMD" ] ; then
    fi
        JAVACMD=$JAVA_HOME/bin/java
    else
        JAVACMD=$JAVA_HOME/jre/sh/java
        # IBM's JDK on AIX uses strange locations for the executables
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
if [ -n "$JAVA_HOME" ] ; then
# Determine the Java command to use to start the JVM.


CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

esac
  NONSTOP* )        nonstop=true ;;
  MSYS* | MINGW* )  msys=true    ;; #(
  Darwin* )         darwin=true  ;; #(
  CYGWIN* )         cygwin=true  ;; #(
case "$( uname )" in                #(
nonstop=false
darwin=false
msys=false
cygwin=false
# OS specific support (must be 'true' or 'false').

} >&2
    exit 1
    echo
    echo "$*"
    echo
die () {

} >&2
    echo "$*"
warn () {

MAX_FD=maximum
# Use the maximum available, or set MAX_FD != -1 to use that value.

APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || exit
APP_BASE_NAME=${0##*/}
# shellcheck disable=SC2034
# This is normally unused

done
    esac
      *)    app_path=$APP_HOME$link ;;
      /*)   app_path=$link ;; #(
    case $link in             #(
    link=${ls#*' -> '}
    ls=$( ls -ld "$app_path" )
do
    [ -h "$app_path" ]
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
while
# Need this for daisy-chained symlinks.

app_path=$0
# Resolve links: $0 may be a link

# Attempt to set APP_HOME

##############################################################################
#
#       You can find Gradle at https://github.com/gradle/gradle/.
#
#       within the Gradle project.
#       https://github.com/gradle/gradle/blob/HEAD/subprojects/plugins/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
#   (3) This script is generated from the Groovy template
#
#       Darwin, MinGW, and NonStop.
#       There are tweaks for specific operating systems such as AIX, CygWin,
#
#       see the in-line comments for details.
#       and GRADLE_OPTS) rely on word-splitting, this is performed explicitly;
#       Where the inherited environment variables (DEFAULT_JVM_OPTS, JAVA_OPTS,
#
#       options in "$@", and eventually passing that to Java.
#       problems, so this is (mostly) avoided, by progressively accumulating
#       space-separated string is a well documented source of bugs and security
#       The "traditional" practice of packing multiple parameters into a
#
#       by Bash, Ksh, etc; in particular arrays are avoided.
#   (2) This script targets any POSIX shell, so it avoids extensions provided
#
#   Important for patching:
#
#         * various built-in commands including «command», «set», and «ulimit».
#         * compound commands having a testable exit status, especially «case»;
#           «${var#prefix}», «${var%suffix}», and «$( cmd )»;
#         * expansions «$var», «${var}», «${var:-default}», «${var+SET}»,
#         * functions;
#       requires all of these POSIX shell features:
#       Busybox and similar reduced shells will NOT work, because this script
#
#           ksh Gradle
#
#       command line, like:
#       bash, then to run this script, type that shell name before the whole
#       noncompliant, but you have some other compliant shell such as ksh or
#   (1) You need a POSIX-compliant shell to run this script. If your /bin/sh is
#
#   Important for running:
#
#   Gradle start up script for POSIX generated by Gradle.
#
##############################################################################

#
# limitations under the License.
# See the License for the specific language governing permissions and
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# distributed under the License is distributed on an "AS IS" BASIS,
# Unless required by applicable law or agreed to in writing, software
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# You may obtain a copy of the License at
# you may not use this file except in compliance with the License.
# Licensed under the Apache License, Version 2.0 (the "License");
#
# Copyright © 2015-2021 the original authors.
#


