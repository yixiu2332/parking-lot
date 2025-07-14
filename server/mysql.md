-- 管理员表 (admin)
CREATE TABLE admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '管理员用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    phone VARCHAR(15) COMMENT '联系电话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '账户创建时间'
) ENGINE=InnoDB COMMENT='管理员表';

-- 用户表 (user)
CREATE TABLE user (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '用户名',
    account VARCHAR(100) UNIQUE NOT NULL COMMENT '账号',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    phone VARCHAR(15) UNIQUE NOT NULL COMMENT '联系电话',
    image LONGBLOB COMMENT '用户头像（二进制文件）',
    license_plate VARCHAR(20) UNIQUE COMMENT '车牌号',
    balance DECIMAL(10, 2) DEFAULT 0 COMMENT '账户余额',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='用户表';

-- 停车场表 (parking_lot)
CREATE TABLE parking_lot (
    parking_lot_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '停车场ID',
    name VARCHAR(100) NOT NULL COMMENT '停车场名称',
    address VARCHAR(255) NOT NULL COMMENT '停车场地址',
    total_spaces INT NOT NULL COMMENT '停车位总数',
    available_spaces INT NOT NULL COMMENT '当前空闲停车位数',
    rate1 DECIMAL(10, 2) NOT NULL COMMENT '首小时费率(元/15分钟)',
    rate2 DECIMAL(10, 2) NOT NULL COMMENT '后续费率(元/15分钟)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间'
) ENGINE=InnoDB COMMENT='停车场表';

-- 停车位表 (parking_space)
CREATE TABLE parking_space (
    space_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '停车位ID',
    parking_lot_id INT NOT NULL COMMENT '所属停车场ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '停车位状态（0-空闲，1-已占用）',
    space_number VARCHAR(50) NOT NULL COMMENT '停车位编号',
    lock_status TINYINT NOT NULL DEFAULT 0 COMMENT '车位锁状态（0-未锁定，1-已锁定）',
    reserved_by VARCHAR(100) DEFAULT NULL COMMENT '预约用户的account',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态最后更新时间',
    FOREIGN KEY (parking_lot_id) REFERENCES parking_lot(parking_lot_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='停车位表';

-- 停车记录表 (parking_record)
CREATE TABLE parking_record (
    record_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id INT NOT NULL COMMENT '用户ID',
    space_id INT NOT NULL COMMENT '停车位ID',
    entry_time DATETIME NOT NULL COMMENT '入场时间',
    exit_time DATETIME DEFAULT NULL COMMENT '离场时间',
    cost DECIMAL(10, 2) DEFAULT 0 COMMENT '停车费用',
    paid TINYINT NOT NULL DEFAULT 0 COMMENT '是否已支付（0-未支付，1-已支付）',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (space_id) REFERENCES parking_space(space_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='停车记录表';

-- 预约表 (reservation)
CREATE TABLE reservation (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '预约ID',
    user_id INT NOT NULL COMMENT '预约用户ID',
    space_id INT NOT NULL COMMENT '预约的停车位ID',
    reservation_time DATETIME NOT NULL COMMENT '预约时间',
    start_time DATETIME NOT NULL COMMENT '预约开始时间',
    end_time DATETIME NOT NULL COMMENT '预约结束时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '预约状态（0-未开始，1-进行中，2-已完成，3-已取消）',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (space_id) REFERENCES parking_space(space_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='预约表';

-- 支付记录表 (payment)
CREATE TABLE payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '支付记录ID',
    user_id INT NOT NULL COMMENT '支付用户ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    payment_method VARCHAR(50) NOT NULL COMMENT '支付方式（如支付宝、微信、信用卡）',
    payment_time DATETIME NOT NULL COMMENT '支付时间',
    order_id INT DEFAULT NULL COMMENT '关联的订单ID',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='支付记录表';

-- 车牌审核表 (license_review)
CREATE TABLE license_review (
    review_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '审核ID',
    user_id INT NOT NULL COMMENT '用户ID',
    license_plate VARCHAR(20) NOT NULL COMMENT '车牌号',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态（0-待审核，1-通过，2-拒绝）',
    submitted_at DATETIME NOT NULL COMMENT '提交时间',
    reviewed_at DATETIME DEFAULT NULL COMMENT '审核时间',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='车牌审核表';

-- 系统日志表 (system_log)
CREATE TABLE system_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    admin_id INT DEFAULT NULL COMMENT '操作管理员ID',
    action VARCHAR(255) NOT NULL COMMENT '操作描述',
    timestamp DATETIME NOT NULL COMMENT '操作时间',
    FOREIGN KEY (admin_id) REFERENCES admin(admin_id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='系统日志表';