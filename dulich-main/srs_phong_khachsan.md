# Tài liệu SRS - Phần Quản lý Phòng (Thay thế Quản lý Khách sạn)

## 1. Giới thiệu
Hệ thống quản lý khách sạn hiện tại chỉ phục vụ cho **một khách sạn duy nhất**. Do đó, chức năng "Quản lý khách sạn" được thay thế bằng chức năng **"Quản lý Phòng"**, nhằm liệt kê và thao tác với các phòng của khách sạn này.

## 2. Mục tiêu
Cung cấp hệ thống quản lý chi tiết các **phòng trong khách sạn**, bao gồm thông tin phòng, loại phòng, tình trạng, giá cả,... phục vụ cho việc đặt phòng, cập nhật trạng thái và theo dõi hiệu suất.

## 3. Phạm vi hệ thống
Thay thế module "Khách sạn" hiện tại bằng module "Phòng" với các tính năng tương đương:
- Quản lý danh sách phòng (CRUD)
- Quản lý loại phòng (CRUD)
- Giao diện người dùng thân thiện, kế thừa style sẵn có từ module cũ

## 4. Chức năng chính

### 4.1. Quản lý Phòng
- **Xem danh sách phòng**: hiển thị tất cả phòng của khách sạn với các thông tin:
  - Mã phòng
  - Tên phòng
  - Loại phòng
  - Giá theo đêm
  - Tình trạng (Trống / Đang thuê / Đang dọn dẹp)
- **Thêm phòng mới**: nhập thông tin và gán loại phòng
- **Sửa thông tin phòng**: chỉnh sửa tên, giá, trạng thái,...
- **Xóa phòng**: xoá phòng không còn sử dụng

### 4.2. Quản lý Loại Phòng
- **Xem danh sách loại phòng**: ví dụ: Phòng đơn, Phòng đôi, Suite, v.v.
- **Thêm loại phòng mới**
- **Chỉnh sửa loại phòng**
- **Xoá loại phòng**

## 5. Vai trò người dùng
Các vai trò trong hệ thống có quyền khác nhau:
- **Admin / Quản lý**:
  - Toàn quyền quản lý phòng và loại phòng
- **Lễ tân**:
  - Xem danh sách phòng và tình trạng phòng để phục vụ việc đặt phòng
  - Không có quyền xoá hoặc sửa loại phòng

## 6. Ràng buộc dữ liệu
- Tên phòng không được trùng lặp trong khách sạn
- Mỗi phòng phải gán với 1 loại phòng
- Loại phòng không thể xoá nếu đang được sử dụng bởi ít nhất một phòng

## 7. Giao diện
Giao diện kế thừa từ module cũ ("Khách sạn") với một số điều chỉnh nhỏ về nội dung hiển thị và biểu tượng.

## 8. Các điểm cần cập nhật
- Đổi toàn bộ text: "Khách sạn" → "Phòng"
- Đổi tên biến/code: `hotel` → `room`, `hotelType` → `roomType`
- Đảm bảo logic validation và xử lý dữ liệu tương thích với mô hình phòng

---
*Phiên bản: 1.0 - Ngày cập nhật: 02/07/2025*
