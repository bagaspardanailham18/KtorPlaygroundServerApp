package com.bagaspardanailham.bpiktorplayground

// Pengecualian kustom untuk validasi input
class ValidationException(message: String) : Exception(message)

// Pengecualian kustom untuk data yang tidak ditemukan
class DataNotFoundException(message: String) : Exception(message)