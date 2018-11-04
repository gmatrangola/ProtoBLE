package com.electrazoom.rpc.ble;

/**
 * Handle invalid use of the API
 */
class InvalidOperation extends RuntimeException {
    public InvalidOperation(String message) {
        super(message);
    }
}
