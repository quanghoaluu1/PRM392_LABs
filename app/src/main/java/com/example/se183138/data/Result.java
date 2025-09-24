package com.example.se183138.data;

public class Result<T> {
    public static class Success<T> extends Result<T>{
        private T data;

        public Success(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }

    public static class Error<T> extends Result<T> {
        private Exception error;

        public Error(Exception error) {
            this.error = error;
        }

        public Exception getError() {
            return error;
        }
    }
}
