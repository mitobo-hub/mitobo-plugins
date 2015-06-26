#! /bin/sh

# Shell script for starting MiToBo on Linux #
#############################################

## set this variable to the directory where you unpacked the Mi_To_Bo.zip
MITOBO_HOME=

if [ -z "$MITOBO_HOME" ] ; then
	## assume current working directory
	MITOBO_HOME=$PWD
fi

## determine machine type and set LD_LIBRARY_PATH
LD=
if [ -z "$LD" ] ; then
	ARCH=`uname -m`
	case "$ARCH" in
		i386)	LD=$MITOBO_HOME/lib/lib32
			;;
		i686)	LD=$MITOBO_HOME/lib/lib32
			;;
		x86_64)	LD=$MITOBO_HOME/lib/lib64
			;;
		*)	echo "can not determine processor architecture: $ARCH"
			;;
	esac
fi

if [ -z "$LD_LIBRARY_PATH" ] ; then
        LD_LIBRARY_PATH=/usr/lib:$LD
else
	LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$LD"
fi


JARS="$MITOBO_HOME/plugins/mitobo_plugins.jar"
for jar in $MITOBO_HOME/plugins/jars/*jar ; do
	JARS="$JARS":"$jar"
done

CLASSPATH=$JARS

export LD_LIBRARY_PATH
export CLASSPATH

# Ok, let's start MiToBo!
java -Dalida.versionprovider_class=de.unihalle.informatik.MiToBo.core.operator.MTBVersionProviderReleaseFile ij.ImageJ -ijpath $MITOBO_HOME
