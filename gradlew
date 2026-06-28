#!/bin/sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
#
#   Gradle Wrapper script for Unix systems
#
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
APP_PATH=`dirname "$0"`
APP_HOME=`cd "${APP_PATH}" && pwd`

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS=""

APP_HOME=`cd "${APP_PATH}" && pwd -P || cd "${APP_PATH}" && pwd`
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, which is /proc/partitions
if [ -n "$CYGWIN" ] ; then
    case `$HOSTTYPE` in
        x86) APP_OPTS="" ;;
        *) APP_OPTS="" ;;
    esac
else
    APP_OPTS=""
fi

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
case "`uname`" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
esac

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"

# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> .*$'`
    if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
    else
    PRG=`dirname "$PRG"`"/$link"
    fi
done

APP_PATH=`dirname "$PRG"`
APP_HOME=`cd "${APP_PATH}" && pwd -P || cd "${APP_PATH}" && pwd`

APP_BASE_NAME=`basename "$0"`

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use (from JAVA_HOME or the path)
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation, or install a JDK."
fi

# Increase the maximum file descriptors if we can
if [ "$cygwin" = "false" -a "$darwin" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD_LIMIT" != "unlimited" ] ; then
            ulimit -n $MAX_FD_LIMIT
        fi
    else
        warn "Could not query maximum file descriptor limit"
    fi
fi

# Collect all arguments for the java command, following the shell quoting/escaping rules
eval APP_ARGS=("${APP_ARGS[@]}")
for arg in "$@" ; do
    APP_ARGS=("${APP_ARGS[@]}" "$arg")
done

# Escape application args
escaped_args=""
for arg in "${APP_ARGS[@]}" ; do
    arg=`echo "$arg" | sed 's/"/\\\\"/' | sed 's/#/\\#/' | sed 's/!\\
/'   `
    escaped_args="$escaped_args \"$arg\""
done

# Load the wrapper JVM options from disk
if [ -f "$APP_HOME/gradle/jvm.options" ] ; then
    JVM_OPTIONS=`cat "$APP_HOME/gradle/jvm.options"`
fi

# Collect the Java options
JAVA_OPTS="$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS $JVM_OPTIONS"

# Wrap the Java options in double quotes if they contain spaces
if [ "$JAVA_OPTS" != "" ] ; then
    JAVA_OPTS="\"$JAVA_OPTS\""
fi

exec "$JAVACMD" $JAVA_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
