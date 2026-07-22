package com.mapnaom.ticketingplatform.dto.access;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record PermissionStatusDto(
        @JsonProperty("ACCESS") Access ACCESS,
        @JsonProperty("TICKET") Ticket TICKET,
        @JsonProperty("CUSTOMER") Customer CUSTOMER,
        @JsonProperty("TEAM_MEMBER") TeamMember TEAM_MEMBER,
        @JsonProperty("TEAM_MANAGER") TeamManager TEAM_MANAGER,
        @JsonProperty("TEAM") ResourcePermissions TEAM,
        @JsonProperty("MEETING") ResourcePermissions MEETING,
        @JsonProperty("TASK") ResourcePermissions TASK,
        @JsonProperty("SLA") Sla SLA
) {
    /*
     Permission Status Tree View with Application-Aligned Persian Descriptions:
     ├── ACCESS (دسترسی)
     │   └── ADMIN (مدیریت دسترسی‌ها)
     │       └── get (مشاهده و مدیریت مجوزها، نقش‌ها، اعطاهای کاربری و محدوده‌های دسترسی)
     ├── TICKET (تیکت)
     │   ├── CREATE (ثبت تیکت پشتیبانی جدید)
     │   ├── READ (مشاهده لیست، جزئیات، پیام‌ها و پیوست‌های تیکت‌های پشتیبانی)
     │   ├── UPDATE (ویرایش تیکت، تغییر وضعیت و اولویت، و ارجاع آن به عضو تیم)
     │   └── DELETE (حذف تیکت پشتیبانی)
     ├── CUSTOMER (مشتری)
     │   ├── READ (مشاهده لیست، پروفایل و سوابق مشتریان)
     │   ├── CREATE (ثبت مشتری جدید و ایجاد حساب کاربری او)
     │   ├── UPDATE (ویرایش پروفایل، اطلاعات تماس و تصویر مشتری)
     │   └── DELETE (حذف حساب مشتری)
     ├── TEAM_MEMBER (عضو تیم)
     │   ├── READ (مشاهده لیست و پروفایل اعضای تیم، سمت شغلی و وضعیت دسترس‌پذیری)
     │   └── UPDATE (ویرایش پروفایل، سمت شغلی، وضعیت دسترس‌پذیری و تصویر عضو تیم)
     ├── TEAM_MANAGER (مدیر تیم)
     │   └── READ (مشاهده لیست و پروفایل مدیران تیم)
     ├── TEAM (تیم)
     │   ├── CREATE (ایجاد تیم جدید با نام، توضیحات و مدیر تیم)
     │   ├── READ (مشاهده لیست، جزئیات و اعضای تیم‌ها)
     │   ├── UPDATE (مدیریت اعضا و نقش آن‌ها در تیم)
     │   └── DELETE (حذف تیم)
     ├── MEETING (جلسه)
     │   ├── CREATE (ایجاد جلسه برای تیم با عنوان، زمان، مکان و شرکت‌کنندگان)
     │   ├── READ (مشاهده جلسات تیم، جلسات پیش‌روی کاربر، دستور جلسه و یادداشت‌ها)
     │   ├── UPDATE (ویرایش جلسه، لغو آن، مدیریت شرکت‌کنندگان، حضوروغیاب، دستور جلسه و یادداشت‌ها)
     │   └── DELETE (حذف جلسه)
     ├── TASK (وظیفه)
     │   ├── CREATE (ایجاد وظیفه و اتصال آن به جلسه و مسئول انجام)
     │   ├── READ (مشاهده، جست‌وجو و پیگیری وظایف و وظایف مربوط به هر جلسه)
     │   ├── UPDATE (ویرایش عنوان، توضیحات، مسئول، مهلت و وضعیت وظیفه)
     │   └── DELETE (حذف وظیفه)
     └── SLA (قرارداد سطح خدمات)
         ├── READ (مشاهده قراردادهای SLA، مشتری، دسته‌بندی، دامنه خدمات و زمان پاسخ‌گویی)
         ├── CREATE (ایجاد قرارداد SLA برای مشتری و دسته‌بندی خدمات)
         ├── UPDATE (ویرایش نام، دامنه خدمات، زمان پاسخ‌گویی و وضعیت فعال قرارداد)
         └── DELETE (حذف قرارداد SLA)
    */


    public static PermissionStatusDto from(Set<String> permissionCodes) {
        return new PermissionStatusDto(
                new Access(new AccessAdmin(has(permissionCodes, "ACCESS_ADMIN"))),
                new Ticket(
                        has(permissionCodes, "TICKET_CREATE"),
                        has(permissionCodes, "TICKET_READ"),
                        has(permissionCodes, "TICKET_UPDATE"),
                        has(permissionCodes, "TICKET_DELETE")),
                new Customer(
                        has(permissionCodes, "CUSTOMER_READ"),
                        has(permissionCodes, "CUSTOMER_CREATE"),
                        has(permissionCodes, "CUSTOMER_UPDATE"),
                        has(permissionCodes, "CUSTOMER_DELETE")),
                new TeamMember(
                        has(permissionCodes, "TEAM_MEMBER_READ"),
                        has(permissionCodes, "TEAM_MEMBER_UPDATE")),
                new TeamManager(has(permissionCodes, "TEAM_MANAGER_READ")),
                resourcePermissions(permissionCodes, "TEAM"),
                resourcePermissions(permissionCodes, "MEETING"),
                resourcePermissions(permissionCodes, "TASK"),
                new Sla(
                        has(permissionCodes, "SLA_READ"),
                        has(permissionCodes, "SLA_CREATE"),
                        has(permissionCodes, "SLA_UPDATE"),
                        has(permissionCodes, "SLA_DELETE"))
        );
    }

    private static boolean has(Set<String> permissionCodes, String permissionCode) {
        return permissionCodes != null && permissionCodes.contains(permissionCode);
    }

    private static ResourcePermissions resourcePermissions(Set<String> permissionCodes, String resource) {
        return new ResourcePermissions(
                has(permissionCodes, resource + "_CREATE"),
                has(permissionCodes, resource + "_READ"),
                has(permissionCodes, resource + "_UPDATE"),
                has(permissionCodes, resource + "_DELETE"));
    }

    public record Access(@JsonProperty("ADMIN") AccessAdmin ADMIN) {
    }

    public record AccessAdmin(@JsonProperty("get") boolean get) {
    }

    public record Ticket(
            @JsonProperty("CREATE") boolean CREATE,
            @JsonProperty("READ") boolean READ,
            @JsonProperty("UPDATE") boolean UPDATE,
            @JsonProperty("DELETE") boolean DELETE
    ) {
    }

    public record Customer(
            @JsonProperty("READ") boolean READ,
            @JsonProperty("CREATE") boolean CREATE,
            @JsonProperty("UPDATE") boolean UPDATE,
            @JsonProperty("DELETE") boolean DELETE
    ) {
    }

    public record TeamMember(
            @JsonProperty("READ") boolean READ,
            @JsonProperty("UPDATE") boolean UPDATE
    ) {
    }

    public record TeamManager(@JsonProperty("READ") boolean READ) {
    }

    public record ResourcePermissions(
            @JsonProperty("CREATE") boolean CREATE,
            @JsonProperty("READ") boolean READ,
            @JsonProperty("UPDATE") boolean UPDATE,
            @JsonProperty("DELETE") boolean DELETE
    ) {
    }

    public record Sla(
            @JsonProperty("READ") boolean READ,
            @JsonProperty("CREATE") boolean CREATE,
            @JsonProperty("UPDATE") boolean UPDATE,
            @JsonProperty("DELETE") boolean DELETE
    ) {
    }
}
