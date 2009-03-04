# This file is included by the top-level libcore Android.mk.
# It's not a normal makefile, so we don't include CLEAR_VARS
# or BUILD_*_LIBRARY.

LOCAL_SRC_FILES := \
	BidiWrapperInterface.c \
	BreakIteratorInterface.c \
	DecimalFormatInterface.cpp \
	CharacterInterface.c \
	ConverterInterface.c \
	CollationInterface.c \
	RegExInterface.cpp \
	ResourceInterface.cpp \
	RBNFInterface.cpp \
	ErrorCode.c

LOCAL_C_INCLUDES += \
	external/icu4c/common \
	external/icu4c/i18n

# Any shared/static libs that are listed here must also
# be listed in libs/nativehelper/Android.mk.
# TODO: fix this requirement

LOCAL_SHARED_LIBRARIES += \
	libicudata \
	libicuuc \
	libicui18n

LOCAL_STATIC_LIBRARIES +=
