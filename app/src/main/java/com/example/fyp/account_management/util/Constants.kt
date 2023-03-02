package com.example.fyp.account_management.util

class Constants {

    object AuthResult{
        const val SUCCESS_LOGIN = "Login Success"
        const val SUCCESS_UPDATE = "Update Success"
        val ERROR_LOGIN = "Login Error"
        val ERROR_UPDATE = "Update Error"
        val SUCCESS_SIGNUP = "Signup Success"
        val ERROR_SIGNUP = "Signup Error"
        val SUCCESS_EMAIL_SENT = "Email Sent Success"
        const val SUCCESS_UPDATE_PASSWORD = "Password Update Success"
    }

    object Command{
        const val EDIT = "edit"
        const val ADD = "add"
    }
}