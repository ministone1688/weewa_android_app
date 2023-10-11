#pragma once

#include <unistd.h>
#include <string.h>

typedef unsigned long long quint64;

class ByteArray {
private:
    char* _buf;
    size_t _cap;

public:
    ByteArray(size_t size, char init);
    ByteArray(ByteArray& b);
    ByteArray(const ByteArray& b);
    ~ByteArray();

    const char* data() const;
    char* data();
    size_t size() const;
    size_t capacity() const;
    ByteArray left(size_t size) const;
    ByteArray mid(size_t pos, size_t len = -1) const;

    char& operator[](int i);
    ByteArray operator+(const ByteArray& b) const;
};