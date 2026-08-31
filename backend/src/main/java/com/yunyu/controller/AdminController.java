package com.yunyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunyu.common.PageResult;
import com.yunyu.common.Result;
import com.yunyu.dao.*;
import com.yunyu.entity.*;
import com.yunyu.service.AdminService;
import com.yunyu.vo.DashboardChartsVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private FishingVenueDao venueDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PostDao postDao;

    @Autowired
    private FishingSpotDao spotDao;

    @Autowired
    private AdminService adminService;

    private void checkSuperAdmin(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("adminRole");
        if (role == null || role != 1) {
            throw new RuntimeException("权限不足，仅最终管理员可操作");
        }
    }

    private Integer getCurrentAdminId(HttpServletRequest request) {
        return (Integer) request.getAttribute("userId");
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userDao.selectCount(null));
        data.put("venueCount", venueDao.selectCount(new LambdaQueryWrapper<FishingVenue>().eq(FishingVenue::getStatus, 1)));
        data.put("orderCount", orderDao.selectCount(null));
        data.put("postCount", postDao.selectCount(null));
        data.put("productCount", productDao.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));
        data.put("spotCount", spotDao.selectCount(new LambdaQueryWrapper<FishingSpot>().eq(FishingSpot::getStatus, 1)));
        data.put("pendingSpotCount", spotDao.selectCount(new LambdaQueryWrapper<FishingSpot>().eq(FishingSpot::getStatus, 0)));
        data.put("pendingPostCount", postDao.selectCount(new LambdaQueryWrapper<Post>().eq(Post::getStatus, 0)));
        return Result.success(data);
    }

    @GetMapping("/dashboard/charts")
    public Result<DashboardChartsVO> dashboardCharts() {
        DashboardChartsVO vo = new DashboardChartsVO();

        // ---- 1. 年龄分布（真实数据） ----
        List<User> users = userDao.selectList(
            new LambdaQueryWrapper<User>().isNotNull(User::getAge)
        );
        // 按年龄段分组统计
        int[] ageCounts = new int[5]; // 0:18-25, 1:26-35, 2:36-45, 3:46-55, 4:55+
        String[] ageLabels = {"18-25岁", "26-35岁", "36-45岁", "46-55岁", "55岁以上"};
        for (User u : users) {
            Integer age = u.getAge();
            if (age == null) continue;
            if (age <= 25) ageCounts[0]++;
            else if (age <= 35) ageCounts[1]++;
            else if (age <= 45) ageCounts[2]++;
            else if (age <= 55) ageCounts[3]++;
            else ageCounts[4]++;
        }
        List<DashboardChartsVO.AgeItem> ageList = new ArrayList<>();
        for (int i = 0; i < ageLabels.length; i++) {
            ageList.add(new DashboardChartsVO.AgeItem(ageLabels[i], ageCounts[i]));
        }
        vo.setAgeDistribution(ageList);

        // ---- 2. 主要鱼种分布 ----
        List<FishingSpot> spots = spotDao.selectList(
            new LambdaQueryWrapper<FishingSpot>().isNotNull(FishingSpot::getFishSpecies)
        );
        Map<String, Integer> fishCount = new LinkedHashMap<>();
        for (FishingSpot spot : spots) {
            String species = spot.getFishSpecies();
            if (species == null || species.trim().isEmpty()) continue;
            for (String name : species.split("[,，、]")) {
                name = name.trim();
                if (!name.isEmpty()) {
                    fishCount.merge(name, 1, Integer::sum);
                }
            }
        }
        // 按数量降序排列，取前15
        List<DashboardChartsVO.FishSpeciesItem> fishList = fishCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(15)
            .map(e -> new DashboardChartsVO.FishSpeciesItem(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
        vo.setFishSpeciesDistribution(fishList);

        // ---- 3. 地域分布 ----
        List<FishingVenue> venues = venueDao.selectList(
            new LambdaQueryWrapper<FishingVenue>().isNotNull(FishingVenue::getRuleDesc)
        );
        Map<String, Integer> cityCount = new LinkedHashMap<>();
        for (FishingVenue venue : venues) {
            String ruleDesc = venue.getRuleDesc();
            if (ruleDesc == null || ruleDesc.trim().isEmpty()) continue;
            // rule_desc 格式为 "XX周边钓场"，提取城市名
            String city = ruleDesc.replace("周边钓场", "").replace("周边", "").trim();
            if (!city.isEmpty()) {
                cityCount.merge(city, 1, Integer::sum);
            }
        }
        List<DashboardChartsVO.RegionItem> regionList = cityCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .map(e -> new DashboardChartsVO.RegionItem(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
        vo.setRegionDistribution(regionList);

        // ---- 4. 钓法分布 ----
        List<Post> posts = postDao.selectList(null);
        Map<Integer, Integer> typeCount = new HashMap<>();
        // 帖子类型：1渔获/2装备/3钓法/4比赛/5求助
        Map<Integer, String> typeNames = new LinkedHashMap<>();
        typeNames.put(1, "渔获分享");
        typeNames.put(2, "装备讨论");
        typeNames.put(3, "钓法交流");
        typeNames.put(4, "比赛活动");
        typeNames.put(5, "求助问答");
        for (Post post : posts) {
            Integer t = post.getType();
            if (t != null) {
                typeCount.merge(t, 1, Integer::sum);
            }
        }
        List<DashboardChartsVO.MethodItem> methodList = typeNames.entrySet().stream()
            .map(e -> new DashboardChartsVO.MethodItem(e.getValue(),
                typeCount.getOrDefault(e.getKey(), 0)))
            .collect(Collectors.toList());
        vo.setMethodDistribution(methodList);

        return Result.success(vo);
    }

    // ========== 用户管理 ==========
    @GetMapping("/users")
    public Result<PageResult<User>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<User> p = new Page<>(page, size);
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(User::getAccount, keyword).or().like(User::getNickname, keyword);
        }
        w.orderByDesc(User::getCreateTime);
        return Result.success(PageResult.from(userDao.selectPage(p, w)));
    }

    @PutMapping("/user/lock/{id}")
    public Result<Void> lockUser(@PathVariable int id, @RequestParam String state) {
        User user = userDao.selectById(id);
        if (user != null) {
            user.setLockState(state);
            userDao.updateById(user);
        }
        return Result.success();
    }

    // ========== 钓场管理 ==========
    @GetMapping("/venues")
    public Result<PageResult<FishingVenue>> getVenues(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FishingVenue> p = new Page<>(page, size);
        return Result.success(PageResult.from(venueDao.selectPage(p, new LambdaQueryWrapper<FishingVenue>()
                .orderByDesc(FishingVenue::getCreateTime))));
    }

    @PutMapping("/venue/status/{id}")
    public Result<Void> updateVenueStatus(@PathVariable int id, @RequestParam int status) {
        FishingVenue v = venueDao.selectById(id);
        if (v != null) {
            v.setStatus(status);
            venueDao.updateById(v);
        }
        return Result.success();
    }

    @DeleteMapping("/venue/{id}")
    public Result<Void> deleteVenue(@PathVariable int id) {
        venueDao.deleteById(id);
        return Result.success();
    }

    // ========== 钓点管理 ==========
    @GetMapping("/spots")
    public Result<PageResult<FishingSpot>> getSpots(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<FishingSpot> p = new Page<>(page, size);
        LambdaQueryWrapper<FishingSpot> w = new LambdaQueryWrapper<>();
        if (status != null) {
            w.eq(FishingSpot::getStatus, status);
        }
        w.orderByDesc(FishingSpot::getCreateTime);
        return Result.success(PageResult.from(spotDao.selectPage(p, w)));
    }

    @PutMapping("/spot/status/{id}")
    public Result<Void> updateSpotStatus(@PathVariable int id, @RequestParam int status) {
        FishingSpot spot = spotDao.selectById(id);
        if (spot != null) {
            spot.setStatus(status);
            spotDao.updateById(spot);
        }
        return Result.success();
    }

    @DeleteMapping("/spot/{id}")
    public Result<Void> deleteSpot(@PathVariable int id) {
        spotDao.deleteById(id);
        return Result.success();
    }

    // ========== 帖子管理 ==========
    @GetMapping("/posts")
    public Result<PageResult<Post>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> w = new LambdaQueryWrapper<>();
        if (status != null) {
            w.eq(Post::getStatus, status);
        }
        w.orderByDesc(Post::getCreateTime);
        return Result.success(PageResult.from(postDao.selectPage(p, w)));
    }

    @PutMapping("/post/status/{id}")
    public Result<Void> updatePostStatus(@PathVariable int id, @RequestParam int status) {
        Post post = postDao.selectById(id);
        if (post != null) {
            post.setStatus(status);
            postDao.updateById(post);
        }
        return Result.success();
    }

    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable int id) {
        postDao.deleteById(id);
        return Result.success();
    }

    // ========== 商品管理 ==========
    @GetMapping("/products")
    public Result<PageResult<Product>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> p = new Page<>(page, size);
        return Result.success(PageResult.from(productDao.selectPage(p, new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreateTime))));
    }

    @PostMapping("/product")
    public Result<Product> createProduct(@RequestBody Product product) {
        product.setSalesCount(0);
        if (product.getStatus() == null) product.setStatus(1);
        productDao.insert(product);
        return Result.success(product);
    }

    @PutMapping("/product/{id}")
    public Result<Void> updateProduct(@PathVariable int id, @RequestBody Product product) {
        product.setId(id);
        productDao.updateById(product);
        return Result.success();
    }

    @PutMapping("/product/status/{id}")
    public Result<Void> toggleProductStatus(@PathVariable int id, @RequestParam int status) {
        Product p = productDao.selectById(id);
        if (p != null) {
            p.setStatus(status);
            productDao.updateById(p);
        }
        return Result.success();
    }

    @DeleteMapping("/product/{id}")
    public Result<Void> deleteProduct(@PathVariable int id) {
        productDao.deleteById(id);
        return Result.success();
    }

    // ========== 管理员管理（仅最终管理员） ==========
    @GetMapping("/admins")
    public Result<List<Admin>> getAdmins(HttpServletRequest request) {
        checkSuperAdmin(request);
        List<Admin> admins = adminService.listAdmins();
        admins.forEach(a -> a.setPassword(null));
        return Result.success(admins);
    }

    @PostMapping("/admin/create")
    public Result<Void> createAdmin(@RequestParam String account, @RequestParam String password,
                                    @RequestParam String name, @RequestParam(defaultValue = "0") Integer role,
                                    HttpServletRequest request) {
        checkSuperAdmin(request);
        adminService.createAdmin(account, password, name, role);
        return Result.success();
    }

    @DeleteMapping("/admin/delete/{id}")
    public Result<Void> deleteAdmin(@PathVariable Integer id, HttpServletRequest request) {
        checkSuperAdmin(request);
        adminService.deleteAdmin(id, getCurrentAdminId(request));
        return Result.success();
    }
}
