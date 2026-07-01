package com.example.thuedocosplay.entity.enums;

public enum ModerationStatus {
    PENDING,    // Chờ duyệt (mặc định khi user gửi review)
    APPROVED,   // Đã duyệt — hiển thị công khai
    HIDDEN,     // Admin ẩn vì vi phạm nội dung
    FLAGGED     // Bị người dùng khác báo cáo, cần admin xem lại
}