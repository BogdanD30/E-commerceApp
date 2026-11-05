package com.ecomm.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void correctPasswordVerifies() {
        String stored = PasswordUtil.createPasswordHash("s3cret-pass");
        assertTrue(PasswordUtil.verifyPassword("s3cret-pass", stored));
    }

    @Test
    void wrongPasswordIsRejected() {
        String stored = PasswordUtil.createPasswordHash("s3cret-pass");
        assertFalse(PasswordUtil.verifyPassword("S3cret-pass", stored));
    }

    @Test
    void samePasswordGetsDifferentSalts() {
        assertNotEquals(PasswordUtil.createPasswordHash("same"), PasswordUtil.createPasswordHash("same"));
    }

    @Test
    void malformedStoredHashIsRejected() {
        assertFalse(PasswordUtil.verifyPassword("anything", "not-a-valid-hash"));
    }

    @Test
    void seededDemoAccountHashVerifies() {
        String demo = "Wh9GXHuyNcBVkmqLDNKoyA==:GeMjwXKoCJRcNgIBD5Ou1ullCxY+snJ7ASfIvut6/VM=";
        assertTrue(PasswordUtil.verifyPassword("Demo1234", demo));
    }
}
