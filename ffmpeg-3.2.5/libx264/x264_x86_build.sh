# Created by jianxi on 2017/6/4
# https://github.com/mabeijianxi
# mabeijianxi@gmail.com

NDK=D:/Android/sdk/ndk-bundle

PLATFORM=$NDK/platforms/android-14/arch-x86/
TOOLCHAIN=$NDK/toolchains/x86-4.9/prebuilt/windows-x86_64
PREFIX=./android/x86

function build_one
{
./configure \
--prefix=$PREFIX \
--disable-shared \
--enable-static \
--disable-asm \
--enable-pic \
--enable-strip \
--host=i686-linux \
--cross-prefix=$TOOLCHAIN/bin/i686-linux-android- \
--sysroot=$PLATFORM \
--extra-cflags="-Os -fpic" \
--extra-ldflags="" \

make clean
make
make install
}

build_one

