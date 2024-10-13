package com.example.if570_lab_uts_hosea_00000070462.util

import android.content.Context
import es.dmoral.toasty.Toasty

object ToastUtil {
    fun showSuccessToast(context: Context, message: String) {
        Toasty.success(context, message, Toasty.LENGTH_SHORT, true).show()
    }

    fun showErrorToast(context: Context, message: String) {
        Toasty.error(context, message, Toasty.LENGTH_SHORT, true).show()
    }

    fun showInfoToast(context: Context, message: String) {
        Toasty.info(context, message, Toasty.LENGTH_SHORT, true).show()
    }

    fun showWarningToast(context: Context, message: String) {
        Toasty.warning(context, message, Toasty.LENGTH_SHORT, true).show()
    }
}