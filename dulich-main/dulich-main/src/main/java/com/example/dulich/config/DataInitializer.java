package com.example.dulich.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.entity.Article;
import com.example.dulich.entity.BookHotel;
import com.example.dulich.entity.BookHotelRoom;
import com.example.dulich.entity.Category;
import com.example.dulich.entity.Comment;
import com.example.dulich.entity.GroupPermission;
import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.Location;
import com.example.dulich.entity.Permission;
import com.example.dulich.entity.Role;
import com.example.dulich.entity.RoleName;
import com.example.dulich.entity.Room;
import com.example.dulich.entity.RoomType;
import com.example.dulich.entity.Service;
import com.example.dulich.entity.User;
import com.example.dulich.repository.ArticleRepository;
import com.example.dulich.repository.BookHotelRepository;
import com.example.dulich.repository.BookHotelRoomRepository;
import com.example.dulich.repository.CategoryRepository;
import com.example.dulich.repository.CommentRepository;
import com.example.dulich.repository.GroupPermissionRepository;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.LocationRepository;
import com.example.dulich.repository.PermissionRepository;
import com.example.dulich.repository.RoleRepository;
import com.example.dulich.repository.RoomRepository;
import com.example.dulich.repository.RoomTypeRepository;
import com.example.dulich.repository.ServiceRepository;
import com.example.dulich.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private GroupPermissionRepository groupPermissionRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookHotelRepository bookHotelRepository;

    @Autowired
    private BookHotelRoomRepository bookHotelRoomRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeData();
    }

    private void initializeData() {
        // Tạo roles và permissions trước
        createRoles();
        createPermissions();
        createGroupPermissions();

        // Tạo users
        createUsers();

        // Tạo locations
        createLocations();

        // Tạo room types
        createRoomTypes();

        // Tạo hotels
        createHotels();

        // Tạo rooms
        createRooms();

        // Tạo services
        createServices();

        // Tạo categories
        createCategories();

        // Tạo articles
        createArticles();

        // Tạo comments
        createComments();

        // Tạo bookings
        createBookHotels();

        // Tạo book hotel rooms
        createBookHotelRooms();

        System.out.println("=== Dữ liệu mẫu đã được khởi tạo thành công! ===");
    }

    private void createRoles() {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName(RoleName.ADMIN);
            adminRole.setDisplayName("Quản trị viên");
            adminRole.setDescription("Quản trị viên hệ thống");
            roleRepository.save(adminRole);

            Role staffRole = new Role();
            staffRole.setName(RoleName.STAFF);
            staffRole.setDisplayName("Nhân viên");
            staffRole.setDescription("Nhân viên khách sạn");
            roleRepository.save(staffRole);

            Role userRole = new Role();
            userRole.setName(RoleName.USER);
            userRole.setDisplayName("Người dùng");
            userRole.setDescription("Khách hàng sử dụng hệ thống");
            roleRepository.save(userRole);

            System.out.println("✓ Đã tạo 3 roles");
        }
    }

    private void createPermissions() {
        if (permissionRepository.count() == 0) {
            String[] permissions = {
                    "user.view|Xem người dùng|Quyền xem danh sách người dùng",
                    "user.create|Tạo người dùng|Quyền tạo người dùng mới",
                    "user.edit|Sửa người dùng|Quyền chỉnh sửa thông tin người dùng",
                    "user.delete|Xóa người dùng|Quyền xóa người dùng",
                    "hotel.manage|Quản lý khách sạn|Quyền quản lý khách sạn"
            };

            for (String perm : permissions) {
                String[] parts = perm.split("\\|");
                Permission permission = new Permission();
                permission.setName(parts[0]);
                permission.setDisplayName(parts[1]);
                permission.setDescription(parts[2]);
                permissionRepository.save(permission);
            }

            System.out.println("✓ Đã tạo 5 permissions");
        }
    }

    private void createGroupPermissions() {
        if (groupPermissionRepository.count() == 0) {
            String[] groups = {
                    "Quản lý người dùng|Nhóm quyền quản lý người dùng",
                    "Quản lý khách sạn|Nhóm quyền quản lý khách sạn",
                    "Quản lý nội dung|Nhóm quyền quản lý nội dung",
                    "Quản lý đặt phòng|Nhóm quyền quản lý đặt phòng",
                    "Báo cáo thống kê|Nhóm quyền báo cáo và thống kê"
            };

            for (String group : groups) {
                String[] parts = group.split("\\|");
                GroupPermission groupPermission = new GroupPermission();
                groupPermission.setName(parts[0]);
                groupPermission.setDescription(parts[1]);
                groupPermissionRepository.save(groupPermission);
            }

            System.out.println("✓ Đã tạo 5 group permissions");
        }
    }

    private void createUsers() {
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
            Role staffRole = roleRepository.findByName(RoleName.STAFF).orElseThrow();
            Role userRole = roleRepository.findByName(RoleName.USER).orElseThrow();

            // Admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@dulich.com");
            admin.setFullName("Nguyễn Văn Admin");
            admin.setPhone("0123456789");
            admin.setAddress("123 Đường ABC, Quận 1, TP.HCM");
            admin.setEnabled(true);
            admin.getRoles().add(adminRole);
            userRepository.save(admin);

            // Staff user
            User staff = new User();
            staff.setUsername("staff");
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setEmail("staff@dulich.com");
            staff.setFullName("Trần Thị Staff");
            staff.setPhone("0987654321");
            staff.setAddress("456 Đường DEF, Quận 2, TP.HCM");
            staff.setEnabled(true);
            staff.getRoles().add(staffRole);
            userRepository.save(staff);

            // Regular users
            String[] userData = {
                    "user1|user123|user1@gmail.com|Lê Văn User1|0111222333|789 Đường GHI, Quận 3, TP.HCM",
                    "user2|user123|user2@gmail.com|Phạm Thị User2|0444555666|101 Đường JKL, Quận 4, TP.HCM",
                    "user3|user123|user3@gmail.com|Hoàng Văn User3|0777888999|202 Đường MNO, Quận 5, TP.HCM"
            };

            for (String data : userData) {
                String[] parts = data.split("\\|");
                User user = new User();
                user.setUsername(parts[0]);
                user.setPassword(passwordEncoder.encode(parts[1]));
                user.setEmail(parts[2]);
                user.setFullName(parts[3]);
                user.setPhone(parts[4]);
                user.setAddress(parts[5]);
                user.setEnabled(true);
                user.getRoles().add(userRole);
                userRepository.save(user);
            }

            System.out.println("✓ Đã tạo 5 users");
        }
    }

    private void createLocations() {
        if (locationRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").orElseThrow();

            String[] locations = {
                    "Hồ Chí Minh|ho-chi-minh|hcm.jpg|Thành phố năng động nhất Việt Nam|Thành phố Hồ Chí Minh là trung tâm kinh tế, văn hóa lớn nhất của Việt Nam với nhiều địa điểm du lịch hấp dẫn.",
                    "Hà Nội|ha-noi|hanoi.jpg|Thủ đô ngàn năm văn hiến|Hà Nội với nhiều di tích lịch sử, văn hóa độc đáo và ẩm thực phong phú.",
                    "Đà Nẵng|da-nang|danang.jpg|Thành phố đáng sống|Đà Nẵng nổi tiếng với những bãi biển đẹp và cây cầu Vàng nổi tiếng.",
                    "Phú Quốc|phu-quoc|phuquoc.jpg|Đảo Ngọc của Việt Nam|Phú Quốc là hòn đảo lớn nhất Việt Nam với biển xanh cát trắng tuyệt đẹp.",
                    "Sapa|sapa|sapa.jpg|Thị trấn trong mây|Sapa với khí hậu mát mẻ quanh năm và những ruộng bậc thang tuyệt đẹp."
            };

            for (String loc : locations) {
                String[] parts = loc.split("\\|");
                Location location = new Location();
                location.setName(parts[0]);
                location.setSlug(parts[1]);
                location.setImage(parts[2]);
                location.setDescription(parts[3]);
                location.setContent(parts[4]);
                location.setStatus(1);
                location.setUserId(admin.getId());
                locationRepository.save(location);
            }

            System.out.println("✓ Đã tạo 5 locations");
        }
    }

    private void createRoomTypes() {
        if (roomTypeRepository.count() == 0) {
            String[] roomTypes = {
                    "Standard|Phòng tiêu chuẩn với đầy đủ tiện nghi cơ bản|800000|2|Single|25.0|Điều hòa, TV, Tủ lạnh mini|standard1.jpg,standard2.jpg",
                    "Deluxe|Phòng cao cấp với view đẹp và tiện nghi tốt hơn|1200000|2|Double|35.0|Điều hòa, TV LCD, Tủ lạnh, Ban công|deluxe1.jpg,deluxe2.jpg",
                    "Suite|Phòng suite rộng rãi với phòng khách riêng|2000000|4|King|50.0|Điều hòa, TV Smart, Tủ lạnh, Phòng khách, Bồn tắm|suite1.jpg,suite2.jpg",
                    "VIP|Phòng VIP sang trọng nhất với dịch vụ đặc biệt|3500000|6|King|80.0|Điều hòa, TV 55inch, Tủ lạnh, Jacuzzi, Butler service|vip1.jpg,vip2.jpg",
                    "Family|Phòng gia đình rộng rãi cho 4-6 người|1800000|6|Double + Single|60.0|Điều hòa, TV, Tủ lạnh, Khu vực chơi trẻ em|family1.jpg,family2.jpg"
            };

            for (String rt : roomTypes) {
                String[] parts = rt.split("\\|");
                RoomType roomType = new RoomType();
                roomType.setName(parts[0]);
                roomType.setDescription(parts[1]);
                roomType.setBasePrice(Double.parseDouble(parts[2]));
                roomType.setMaxCapacity(Integer.parseInt(parts[3]));
                roomType.setBedType(parts[4]);
                roomType.setRoomSize(Double.parseDouble(parts[5]));
                roomType.setAmenities(parts[6]);
                roomType.setImages(parts[7]);
                roomType.setStatus(1);
                roomTypeRepository.save(roomType);
            }

            System.out.println("✓ Đã tạo 5 room types");
        }
    }

    private void createHotels() {
        if (hotelRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").orElseThrow();
            Location hcm = locationRepository.findBySlug("ho-chi-minh");
            Location hanoi = locationRepository.findBySlug("ha-noi");
            Location danang = locationRepository.findBySlug("da-nang");
            Location phuquoc = locationRepository.findBySlug("phu-quoc");
            Location sapa = locationRepository.findBySlug("sapa");

            String[] hotels = {
                    "Grand Hotel Saigon|grand-saigon.jpg|grand-album.jpg|8 Đồng Khởi, Quận 1, TP.HCM|028-3829-5555|200|1500000|15|Khách sạn sang trọng 5 sao giữa lòng Sài Gòn|Khách sạn Grand Saigon là biểu tượng của sự sang trọng và đẳng cấp...",
                    "Lotte Hotel Hanoi|lotte-hanoi.jpg|lotte-album.jpg|54 Liễu Giai, Ba Đình, Hà Nội|024-3333-1000|300|1800000|20|Khách sạn 5 sao cao cấp tại trung tâm Hà Nội|Lotte Hotel Hanoi mang đến trải nghiệm nghỉ dưỡng đẳng cấp...",
                    "InterContinental Danang|intercontinental-dn.jpg|inter-album.jpg|Bãi Bắc, Sơn Trà, Đà Nẵng|0236-3938-888|180|2200000|25|Resort biển đẳng cấp quốc tế|InterContinental Danang là resort sang trọng bên bờ biển...",
                    "JW Marriott Phu Quoc|marriott-pq.jpg|marriott-album.jpg|Bãi Kem, An Thới, Phú Quốc|0297-3581-888|250|2500000|30|Resort nghỉ dưỡng sang trọng bậc nhất|JW Marriott Phu Quoc mang đến trải nghiệm nghỉ dưỡng tuyệt vời...",
                    "MGallery Hotel Sapa|mgallery-sapa.jpg|mgallery-album.jpg|Fansipan, Sa Pa, Lào Cai|0214-3871-999|120|1200000|10|Khách sạn boutique giữa núi rừng Sapa|MGallery Sapa với thiết kế độc đáo hòa quyện với thiên nhiên..."
            };

            Location[] locationArray = { hcm, hanoi, danang, phuquoc, sapa };

            for (int i = 0; i < hotels.length; i++) {
                String[] parts = hotels[i].split("\\|");
                Hotel h = new Hotel();
                h.setName(parts[0]);
                h.setImage(parts[1]);
                h.setAlbumImage(parts[2]);
                h.setAddress(parts[3]);
                h.setPhone(parts[4]);
                h.setNumberPeople(Integer.parseInt(parts[5]));
                h.setPrice(Double.parseDouble(parts[6]));
                h.setSale(Integer.parseInt(parts[7]));
                h.setDescription(parts[8]);
                h.setContent(parts[9]);
                h.setLocation(locationArray[i]);
                h.setUser(admin);
                h.setStatus(1);
                h.setStartDate(LocalDateTime.now().minusDays(30));
                h.setEndDate(LocalDateTime.now().plusDays(365));
                hotelRepository.save(h);
            }

            System.out.println("✓ Đã tạo 5 hotels");
        }
    }

    private void createRooms() {
        if (roomRepository.count() == 0) {
            List<Hotel> hotels = hotelRepository.findAll();
            List<RoomType> roomTypes = roomTypeRepository.findAll();

            if (hotels.size() >= 2 && roomTypes.size() >= 3) {
                Hotel hotel1 = hotels.get(0); // Grand Hotel Saigon
                Hotel hotel2 = hotels.get(1); // Lotte Hotel Hanoi
                RoomType standard = roomTypes.get(0); // Standard
                RoomType deluxe = roomTypes.get(1); // Deluxe
                RoomType suite = roomTypes.get(2); // Suite

                String[] rooms = {
                        "101|" + hotel1.getId() + "|" + standard.getId()
                                + "|1|2|850000|Phòng Standard tầng 1 với view vườn|Điều hòa, TV, Wifi miễn phí|room101.jpg",
                        "201|" + hotel1.getId() + "|" + deluxe.getId()
                                + "|2|2|1250000|Phòng Deluxe tầng 2 với view thành phố|Điều hòa, TV LCD, Wifi, Ban công|room201.jpg",
                        "301|" + hotel2.getId() + "|" + suite.getId()
                                + "|3|4|2100000|Phòng Suite tầng 3 với view hồ|Điều hòa, TV Smart, Wifi, Phòng khách|room301.jpg",
                        "102|" + hotel1.getId() + "|" + standard.getId()
                                + "|1|2|850000|Phòng Standard tầng 1 view sân|Điều hòa, TV, Wifi miễn phí, Tủ lạnh|room102.jpg",
                        "202|" + hotel2.getId() + "|" + deluxe.getId()
                                + "|2|2|1250000|Phòng Deluxe tầng 2 view công viên|Điều hòa, TV LCD, Wifi, Mini bar|room202.jpg"
                };

                for (String room : rooms) {
                    String[] parts = room.split("\\|");
                    Room r = new Room();
                    r.setRoomNumber(parts[0]);
                    r.setHotel(hotelRepository.findById(Long.parseLong(parts[1])).orElseThrow());
                    r.setRoomType(roomTypeRepository.findById(Long.parseLong(parts[2])).orElseThrow());
                    r.setFloorNumber(Integer.parseInt(parts[3]));
                    r.setCapacity(Integer.parseInt(parts[4]));
                    r.setPricePerNight(Double.parseDouble(parts[5]));
                    r.setDescription(parts[6]);
                    r.setAmenities(parts[7]);
                    r.setImages(parts[8]);
                    r.setStatus(1);
                    roomRepository.save(r);
                }

                System.out.println("✓ Đã tạo 5 rooms");
            }
        }
    }

    private void createServices() {
        if (serviceRepository.count() == 0) {
            String[] services = {
                    "Room Service 24h|Dịch vụ phòng 24 giờ|150000|Room Service|false|true|24/7",
                    "Spa & Massage|Dịch vụ spa và massage thư giãn|500000|Spa|false|false|08:00 - 22:00",
                    "Airport Transfer|Dịch vụ đưa đón sân bay|200000|Transport|false|false|24/7",
                    "Laundry Service|Dịch vụ giặt ủi|80000|Laundry|false|false|06:00 - 20:00",
                    "Fitness Center|Phòng tập gym hiện đại|0|Fitness|true|true|24/7"
            };

            for (String service : services) {
                String[] parts = service.split("\\|");
                Service s = new Service();
                s.setName(parts[0]);
                s.setDescription(parts[1]);
                s.setPrice(Double.parseDouble(parts[2]));
                s.setCategory(parts[3]);
                s.setIsIncluded(Boolean.parseBoolean(parts[4]));
                s.setIsAvailable24h(Boolean.parseBoolean(parts[5]));
                s.setOperatingHours(parts[6]);
                s.setStatus(1);
                s.setSortOrder(0);
                serviceRepository.save(s);
            }

            System.out.println("✓ Đã tạo 5 services");
        }
    }

    private void createCategories() {
        if (categoryRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").orElseThrow();

            String[] categories = {
                    "Du lịch|du-lich|fa-plane|Khám phá du lịch|Chia sẻ kinh nghiệm và địa điểm du lịch hấp dẫn|du lịch, travel, vacation",
                    "Ẩm thực|am-thuc|fa-utensils|Thưởng thức ẩm thực|Khám phá các món ăn ngon và nhà hàng nổi tiếng|ẩm thực, food, restaurant",
                    "Khách sạn|khach-san|fa-bed|Nghỉ dưỡng tuyệt vời|Đánh giá và giới thiệu các khách sạn, resort|khách sạn, hotel, resort",
                    "Văn hóa|van-hoa|fa-landmark|Tìm hiểu văn hóa|Khám phá văn hóa và lịch sử các vùng miền|văn hóa, culture, history",
                    "Mẹo hay|meo-hay|fa-lightbulb|Mẹo du lịch|Những mẹo hay và kinh nghiệm du lịch hữu ích|mẹo hay, tips, experience"
            };

            for (String cat : categories) {
                String[] parts = cat.split("\\|");
                Category c = new Category();
                c.setName(parts[0]);
                c.setSlug(parts[1]);
                c.setIcon(parts[2]);
                c.setTitleSeo(parts[3]);
                c.setDescription(parts[4]);
                c.setKeywordSeo(parts[5]);
                c.setActive(1);
                c.setHome(1);
                c.setUserId(admin.getId());
                categoryRepository.save(c);
            }

            System.out.println("✓ Đã tạo 5 categories");
        }
    }

    private void createArticles() {
        if (articleRepository.count() == 0) {
            User admin = userRepository.findByUsername("admin").orElseThrow();
            List<Category> categories = categoryRepository.findAll();

            if (categories.size() >= 3) {
                Category dulich = categories.get(0); // Du lịch
                Category amthuc = categories.get(1); // Ẩm thực
                Category khachsan = categories.get(2); // Khách sạn

                String[] articles = {
                        "Top 10 địa điểm du lịch Việt Nam|top-10-dia-diem-du-lich-viet-nam|Khám phá 10 địa điểm du lịch đẹp nhất Việt Nam|Việt Nam có rất nhiều địa điểm du lịch tuyệt đẹp từ Bắc vào Nam...|article1.jpg|Top 10 địa điểm du lịch đẹp nhất Việt Nam|địa điểm du lịch, Việt Nam, travel|"
                                + dulich.getId(),
                        "Món ăn đặc sản miền Nam|mon-an-dac-san-mien-nam|Thưởng thức những món ăn đặc sản hấp dẫn|Miền Nam Việt Nam nổi tiếng với những món ăn đặc sản thơm ngon...|article2.jpg|Món ăn đặc sản miền Nam không thể bỏ qua|ẩm thực miền Nam, đặc sản, food|"
                                + amthuc.getId(),
                        "Review khách sạn 5 sao Sài Gòn|review-khach-san-5-sao-sai-gon|Đánh giá chi tiết các khách sạn 5 sao|Sài Gòn có rất nhiều khách sạn 5 sao đẳng cấp quốc tế...|article3.jpg|Review khách sạn 5 sao tại Sài Gòn|khách sạn 5 sao, Sài Gòn, review|"
                                + khachsan.getId(),
                        "Kinh nghiệm du lịch Đà Lạt|kinh-nghiem-du-lich-da-lat|Chia sẻ kinh nghiệm du lịch thành phố ngàn hoa|Đà Lạt là thành phố lãng mạn với khí hậu mát mẻ quanh năm...|article4.jpg|Kinh nghiệm du lịch Đà Lạt từ A đến Z|Đà Lạt, kinh nghiệm du lịch, thành phố ngàn hoa|"
                                + dulich.getId(),
                        "Quán ăn ngon Hà Nội|quan-an-ngon-ha-noi|Khám phá những quán ăn ngon nổi tiếng|Hà Nội có rất nhiều quán ăn ngon với hương vị đặc trưng...|article5.jpg|Quán ăn ngon nổi tiếng tại Hà Nội|quán ăn ngon, Hà Nội, street food|"
                                + amthuc.getId()
                };

                for (String article : articles) {
                    String[] parts = article.split("\\|");
                    Article a = new Article();
                    a.setName(parts[0]);
                    a.setSlug(parts[1]);
                    a.setDescription(parts[2]);
                    a.setContent(parts[3]);
                    a.setAvatar(parts[4]);
                    a.setTitleSeo(parts[5]);
                    a.setKeywordSeo(parts[6]);
                    a.setCategory(categoryRepository.findById(Long.parseLong(parts[7])).orElseThrow());
                    a.setUser(admin);
                    a.setActive(1);
                    a.setHot(1);
                    a.setView(0);
                    articleRepository.save(a);
                }

                System.out.println("✓ Đã tạo 5 articles");
            }
        }
    }

    private void createComments() {
        if (commentRepository.count() == 0) {
            User user1 = userRepository.findByUsername("user1").orElseThrow();
            User user2 = userRepository.findByUsername("user2").orElseThrow();
            User user3 = userRepository.findByUsername("user3").orElseThrow();

            List<Hotel> hotels = hotelRepository.findAll();
            List<Article> articles = articleRepository.findAll();

            if (hotels.size() > 0 && articles.size() > 0) {
                Hotel hotel1 = hotels.get(0);
                Article article1 = articles.get(0);

                String[] comments = {
                        "Khách sạn này rất tuyệt vời, dịch vụ chu đáo và phòng ốc sạch sẽ!|Nguyễn Văn A|nguyenvana@gmail.com|"
                                + user1.getId() + "|" + hotel1.getId() + "|null",
                        "Bài viết rất hữu ích, cảm ơn tác giả đã chia sẻ!|Trần Thị B|tranthib@gmail.com|"
                                + user2.getId() + "|null|" + article1.getId(),
                        "Tôi đã ở khách sạn này và thật sự ấn tượng với view từ phòng!|Lê Văn C|levanc@gmail.com|"
                                + user3.getId() + "|" + hotel1.getId() + "|null",
                        "Những địa điểm trong bài viết đều rất đẹp, sẽ tham khảo cho chuyến đi sắp tới!|Phạm Thị D|phamthid@gmail.com|"
                                + user1.getId() + "|null|" + article1.getId(),
                        "Staff khách sạn rất thân thiện và nhiệt tình, chắc chắn sẽ quay lại!|Hoàng Văn E|hoangvane@gmail.com|"
                                + user2.getId() + "|" + hotel1.getId() + "|null"
                };

                for (String comment : comments) {
                    String[] parts = comment.split("\\|");
                    Comment c = new Comment();
                    c.setContent(parts[0]);
                    c.setName(parts[1]);
                    c.setEmail(parts[2]);
                    c.setUser(userRepository.findById(Long.parseLong(parts[3])).orElseThrow());

                    if (!"null".equals(parts[4])) {
                        c.setHotel(hotelRepository.findById(Long.parseLong(parts[4])).orElseThrow());
                    }
                    if (!"null".equals(parts[5])) {
                        c.setArticle(articleRepository.findById(Long.parseLong(parts[5])).orElseThrow());
                    }
                    c.setStatus(1);
                    commentRepository.save(c);
                }

                System.out.println("✓ Đã tạo 5 comments");
            }
        }
    }

    private void createBookHotels() {
        if (bookHotelRepository.count() == 0) {
            User user1 = userRepository.findByUsername("user1").orElseThrow();
            User user2 = userRepository.findByUsername("user2").orElseThrow();
            User user3 = userRepository.findByUsername("user3").orElseThrow();

            List<Hotel> hotels = hotelRepository.findAll();
            List<RoomType> roomTypes = roomTypeRepository.findAll();
            List<Room> rooms = roomRepository.findAll();

            if (hotels.size() >= 2 && roomTypes.size() >= 2 && rooms.size() >= 2) {
                Hotel hotel1 = hotels.get(0);
                Hotel hotel2 = hotels.get(1);
                RoomType standard = roomTypes.get(0);
                RoomType deluxe = roomTypes.get(1);
                Room room1 = rooms.get(0);
                Room room2 = rooms.get(1);

                LocalDateTime checkIn = LocalDateTime.now().plusDays(7);
                LocalDateTime checkOut = LocalDateTime.now().plusDays(10);

                String[] bookings = {
                        user1.getId() + "|" + hotel1.getId() + "|" + standard.getId() + "|" + room1.getId()
                                + "|Lê Văn User1|user1@gmail.com|0111222333|" + checkIn + "|" + checkOut
                                + "|2|3|2550000|1",
                        user2.getId() + "|" + hotel1.getId() + "|" + deluxe.getId() + "|" + room2.getId()
                                + "|Phạm Thị User2|user2@gmail.com|0444555666|" + checkIn.plusDays(1) + "|"
                                + checkOut.plusDays(1) + "|2|3|3750000|2",
                        user3.getId() + "|" + hotel2.getId() + "|" + standard.getId()
                                + "|null|Hoàng Văn User3|user3@gmail.com|0777888999|" + checkIn.plusDays(2) + "|"
                                + checkOut.plusDays(2) + "|1|3|2550000|1",
                        user1.getId() + "|" + hotel2.getId() + "|" + deluxe.getId()
                                + "|null|Lê Văn User1|user1@gmail.com|0111222333|" + checkIn.plusDays(5) + "|"
                                + checkOut.plusDays(3) + "|2|2|2500000|3",
                        user2.getId() + "|" + hotel1.getId() + "|" + standard.getId()
                                + "|null|Phạm Thị User2|user2@gmail.com|0444555666|" + checkIn.plusDays(10) + "|"
                                + checkOut.plusDays(5) + "|2|4|3400000|1"
                };

                for (String booking : bookings) {
                    String[] parts = booking.split("\\|");
                    BookHotel bh = new BookHotel();
                    bh.setUser(userRepository.findById(Long.parseLong(parts[0])).orElseThrow());
                    bh.setHotel(hotelRepository.findById(Long.parseLong(parts[1])).orElseThrow());
                    bh.setRoomType(roomTypeRepository.findById(Long.parseLong(parts[2])).orElseThrow());

                    if (!"null".equals(parts[3])) {
                        bh.setRoom(roomRepository.findById(Long.parseLong(parts[3])).orElseThrow());
                    }

                    bh.setCustomerName(parts[4]);
                    bh.setEmail(parts[5]);
                    bh.setPhone(parts[6]);
                    bh.setCheckInDate(LocalDateTime.parse(parts[7]));
                    bh.setCheckOutDate(LocalDateTime.parse(parts[8]));
                    bh.setNumberOfGuests(Integer.parseInt(parts[9]));
                    bh.setTotalNights(Integer.parseInt(parts[10]));
                    bh.setTotalPrice(Double.parseDouble(parts[11]));
                    bh.setStatus(Integer.parseInt(parts[12]));

                    bookHotelRepository.save(bh);
                }

                System.out.println("✓ Đã tạo 5 book hotels");
            }
        }
    }

    private void createBookHotelRooms() {
        if (bookHotelRoomRepository.count() == 0) {
            List<BookHotel> bookings = bookHotelRepository.findAll();
            List<Room> rooms = roomRepository.findAll();

            if (bookings.size() >= 5 && rooms.size() >= 5) {
                BookHotelRoom[] bookingRooms = {
                        new BookHotelRoom(null, bookings.get(0), rooms.get(0), null, null),
                        new BookHotelRoom(null, bookings.get(1), rooms.get(1), null, null),
                        new BookHotelRoom(null, bookings.get(2), rooms.get(2), null, null),
                        new BookHotelRoom(null, bookings.get(3), rooms.get(3), null, null),
                        new BookHotelRoom(null, bookings.get(4), rooms.get(4), null, null)
                };
                for (BookHotelRoom bhr : bookingRooms) {
                    bookHotelRoomRepository.save(bhr);
                }

                System.out.println("✓ Đã tạo 5 book hotel rooms");
            }
        }
    }
}
