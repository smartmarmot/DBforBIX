#!/bin/sh

### BEGIN INIT INFO
# Provides:          dbforbix
# Required-Start:    $syslog $network $time
# Required-Stop:     $syslog $network
# Should-Start:      postgresql mysql clamav-daemon greylist spamassassin
# Should-Stop:       postgresql mysql clamav-daemon greylist spamassassin
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: dbforbix daemon
# Description:       dbforbix daemon
### END INIT INFO

# Setup variables
DESC="DBforBIX monitoring daemon"
EXEC=`whereis -b -B /bin /sbin /usr/bin /usr/sbin /usr/local/bin /usr/local/sbin -f jsvc | awk '{ print $2;}'`
BASEDIR="/opt/dbforbix"
#CLASS_PATH="$BASEDIR/dbforbix.jar":"$BASEDIR/.":"$BASEDIR/lib/commons-cli-1.3.1.jar":"$BASEDIR/lib/httpcore-4.4.4.jar":"$BASEDIR/lib/commons-codec-1.9.jar":"$BASEDIR/lib/jtds-1.3.1.jar":"$BASEDIR/lib/commons-collections-3.2.2.jar":"$BASEDIR/lib/log4j-1.2.17.jar":"$BASEDIR/lib/commons-configuration-1.10.jar":"$BASEDIR/lib/logback-classic-1.0.13.jar":"$BASEDIR/lib/commons-daemon-1.0.15.jar":"$BASEDIR/lib/logback-core-1.0.13.jar":"$BASEDIR/lib/commons-dbcp-1.4.jar":"$BASEDIR/lib/mysql-connector-java-5.1.38.jar":"$BASEDIR/lib/commons-lang-2.6.jar":"$BASEDIR/lib/ojdbc6.jar":"$BASEDIR/lib/commons-logging-1.2.jar":"$BASEDIR/lib/postgresql-9.4.1207.jre7.jar":"$BASEDIR/lib/commons-pool-1.6.jar":"$BASEDIR/lib/slf4j-api-1.7.5.jar":"$BASEDIR/lib/dom4j-1.6.1.jar":"$BASEDIR/lib/xml-apis-1.0.b2.jar":"$BASEDIR/lib/fastjson-1.2.8.jar":"$BASEDIR/lib/zabbix-sender-0.0.1.jar":"$BASEDIR/lib/httpclient-4.5.2.jar"
CLASS_PATH="$BASEDIR/*":"$BASEDIR/lib/*"

CLASS=com.smartmarmot.dbforbix.DBforBix
USER=dbforbix
PID=/tmp/dbforbix.pid
LOG_OUT=/var/log/dbforbix.out
LOG_ERR=/var/log/dbforbix.err

if [ "$EXEC" = "" ]; then
    echo "jsvc not found, terminating"
    exit 3
fi

if [ -f /opt/dbforbix/java_env ]; then
    . /opt/dbforbix/java_env
fi

# try to find java home if not defined
if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
#    echo "JAVA_HOME and JRE_HOME not defined"
    if [ -x /etc/alternatives/java ]; then
	LINK=`readlink -f /etc/alternatives/java`
	LINK="${LINK%bin/java}"
	export JAVA_HOME=$LINK
    fi
fi

do_exec()
{
    CURDIR=`pwd`
    JSVCV=`$EXEC -? | grep "jsvc (Apache Commons Daemon)" | awk '{ print $5;}'`
    JSVCV=${JSVCV%-*}
    MAIN=${JSVCV%%.*}
    MINOR=${JSVCV%.*}
    MINOR=${MINOR#*.}
    PATCH=${JSVCV##*.}
    
    # they added -cwd with patchlevel 1.0.11
    if [ $PATCH -ge 11 ]; then
	$EXEC -cwd $BASEDIR -Xmx64m -debug -procname dbforbix -cp $CLASS_PATH -user $USER -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $CLASS $BASEDIR
    else
	echo "Using compartibility for jsvc < 1.0.11"
	cd $BASEDIR
	echo $BASEDIR
	$EXEC -Xmx64m -debug -procname dbforbix -cp $CLASS_PATH -user $USER -outfile $LOG_OUT -errfile $LOG_ERR -pidfile $PID $1 $CLASS $BASEDIR
	cd $CURDIR
    fi
}

case "$1" in
    start)
	echo -n "Starting $DESC... "
        do_exec ""
	echo "DONE"
        ;;

    stop)
	echo -n "Stopping $DESC... "
        do_exec "-stop"
	echo "DONE"
        ;;

    restart)
        if [ -f "$PID" ]; then
            do_exec "-stop"
	    sleep 1
            do_exec
        else
            echo "service not running, will do nothing"
            exit 1
        fi
        ;;

    status)
	echo -n "Status of $DESC: "
	PCOUNT=`ps aux | grep "dbforbix" | wc -l`
	if [ "$PCOUNT" -gt 3 ]; then
	    echo "most likely runnnig"
	    exit 0
	else
	    echo "most likely stopped"
	    exit 3
	fi    
        ;;

    *)
        echo "usage: $0 {start|stop|restart}" >&2
        exit 9
        ;;
esac
