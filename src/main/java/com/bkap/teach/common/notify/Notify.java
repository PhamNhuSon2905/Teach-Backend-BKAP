package com.bkap.teach.common.notify;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public final class Notify {

    private static final String KEY = "notify";

    private Notify() {}


    public static void success(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute(KEY, new Notification(NotifyType.SUCCESS, msg));
    }

    public static void error(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute(KEY, new Notification(NotifyType.ERROR, msg));
    }

    public static void warning(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute(KEY, new Notification(NotifyType.WARNING, msg));
    }

    public static void info(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute(KEY, new Notification(NotifyType.INFO, msg));
    }


    public static void success(Model model, String msg) {
        model.addAttribute(KEY, new Notification(NotifyType.SUCCESS, msg));
    }

    public static void error(Model model, String msg) {
        model.addAttribute(KEY, new Notification(NotifyType.ERROR, msg));
    }

    public static void warning(Model model, String msg) {
        model.addAttribute(KEY, new Notification(NotifyType.WARNING, msg));
    }

    public static void info(Model model, String msg) {
        model.addAttribute(KEY, new Notification(NotifyType.INFO, msg));
    }

}
