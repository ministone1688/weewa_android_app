#include "byte_array.h"
#include <stdlib.h>

ByteArray::ByteArray(size_t size, char init) {
    _buf = (char *) malloc(size + 1);
    _cap = size + 1;
    memset(_buf, init, size);
    _buf[size] = 0;
}

ByteArray::ByteArray(ByteArray &b) {
    _cap = b.capacity();
    _buf = (char *) malloc(_cap);
    memcpy(_buf, b.data(), _cap);
}

ByteArray::ByteArray(const ByteArray &b) {
    _cap = b.capacity();
    _buf = (char *) malloc(_cap);
    memcpy(_buf, b.data(), _cap);
}

ByteArray::~ByteArray() {
    free(_buf);
}

ByteArray ByteArray::left(size_t size) const {
    if(size <= 0) {
        return ByteArray(0, 0);
    }
    if(size > _cap - 1) {
        return ByteArray(*this);
    }
    ByteArray b(size, 0);
    memcpy(b.data(), _buf, size);
    return b;
}

ByteArray ByteArray::mid(size_t pos, size_t len) const {
    if(len == -1 || pos + len > size()) {
        ByteArray b(size() - pos, 0);
        memcpy(b.data(), _buf + pos, b.size());
        return b;
    } else {
        ByteArray b(len, 0);
        memcpy(b.data(), _buf + pos, len);
        return b;
    }
}

const char *ByteArray::data() const {
    return _buf;
}

char *ByteArray::data() {
    return _buf;
}

size_t ByteArray::size() const {
    return _cap - 1;
}

size_t ByteArray::capacity() const {
    return _cap;
}

char& ByteArray::operator[](int i) {
    if(i < 0 || i >= size()) {
        return _buf[0];
    }
    return _buf[i];
}

ByteArray ByteArray::operator+(const ByteArray& b) const {
    ByteArray r(size() + b.size(), 0);
    memcpy(r.data(), _buf, size());
    memcpy(r.data() + size(), b.data(), b.size());
    return r;
}