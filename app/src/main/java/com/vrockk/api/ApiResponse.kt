package com.vrockk.api

import com.google.gson.JsonElement

class ApiResponse (status: Status, data: JsonElement?, error: Throwable?) {
    val status: Status = status
    val data: JsonElement? = data
    val error: Throwable? = error

    companion object {
        fun loading(): ApiResponse {
            return ApiResponse(Status.LOADING, null, null)
        }

        fun success(data: JsonElement): ApiResponse {
            return ApiResponse(Status.SUCCESS, data, null)
        }

        fun error(error: Throwable): ApiResponse {
            return ApiResponse(Status.ERROR, null, error)
        }
    }


}