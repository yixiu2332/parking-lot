package com.zyf.message;

import com.zyf.dao.*;
import com.zyf.model.*;
import com.zyf.service.ParkingTransactionService;
import com.zyf.util.EmptyJSON;
import org.json.JSONObject;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;

public class WebSocketMessageHandler {
    private static volatile WebSocketMessageHandler instance;
    SocketServer socketServer = SocketServer.getInstance();
    private WebSocketMessageHandler() {
        if (instance != null) {
            throw new IllegalStateException("实例已存在");
        }
    }

    public static WebSocketMessageHandler getInstance() {
        if (instance == null) {
            synchronized (WebSocketMessageHandler.class) {
                if (instance == null) {
                    instance = new WebSocketMessageHandler();
                }
            }
        }
        return instance;
    }

    public JSONObject processMessages(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        JSONObject replyRoot = null;
        try {
            JSONObject jsonObject = new JSONObject(message);
            if (!jsonObject.has("data")) {
                System.err.println("无效的消息格式: 缺少data字段");
                return null;
            }

            JSONObject dataObject = jsonObject.getJSONObject("data");
            if (!dataObject.has("describe")) {
                System.err.println("无效的消息格式: 缺少describe字段");
                return null;
            }

            String describe = dataObject.getString("describe");
            switch (describe) {
                case "UserLoginVerification":
                    replyRoot = processUserLoginVerification(dataObject);
                    break;
                case "UserRegister":
                    replyRoot = processUserRegister(dataObject);
                    break;
                case "HomeRequestData":
                    replyRoot = processHomeRequestData(dataObject);
                    break;
                case "CheckParkingSpace":
                    replyRoot = processCheckParkingSpace(dataObject);
                    break;
                case "ReserveAParkingSpace":
                    replyRoot = processReserveAParkingSpace(dataObject);
                    break;
                case "CancelAppointment":
                    replyRoot = processCancelAppointment(dataObject);
                    break;
                case "ParkingLockControl":
                    replyRoot = processParkingLockControl(dataObject);
                    break;
                case "QueryUserInformation":
                    replyRoot = processQueryUserInformation(dataObject);
                    break;
                case "StartTimingSignal":
                    replyRoot = processStartTimingSignal(dataObject);
                    break;
                case "EndTimingSignal":
                    replyRoot = processEndTimingSignal(dataObject);
                    break;
                case "PaySuccess":
                    replyRoot = processPaySuccess(dataObject);
                    break;
                case "UpdateUserInformation":
                    replyRoot = processUpdateUserInformation(dataObject);
                    break;
                case "history":
                    replyRoot = processHistory(dataObject);
                    break;
                default:
                    replyRoot = null;
            }
        } catch (Exception e) {
            System.err.println("处理消息时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return replyRoot;
    }

    private JSONObject processHistory(JSONObject dataObject) {
        int userId = dataObject.getInt("userId");
        ParkingRecordDao parkingRecordDao = new ParkingRecordDao();
        List<ParkingRecord> userParkingRecords = parkingRecordDao.getUserParkingRecords(userId);
        JSONObject root = EmptyJSON.getForData();
        JSONObject data = new JSONObject();
        data.put("describe","history");
        int count = 0;
        for (ParkingRecord parkingRecord : userParkingRecords) {
            count++;
            java.util.Date entryTime = parkingRecord.getEntryTime();
            java.util.Date exitTime = parkingRecord.getExitTime();
            Integer spaceId = parkingRecord.getSpaceId();
            JSONObject record = new JSONObject();
            record.put("space", spaceId);
            Instant instant = entryTime.toInstant();
            LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            record.put("date", localDate.getYear() +"-"+ localDate.getMonthValue() +"-"+ localDate.getDayOfMonth());
            // 计算两个日期之间的时间差（以毫秒为单位）
            long differenceInMillis = exitTime.getTime() - entryTime.getTime();
            // 将时间差转换为分钟
            long differenceInMinutes = differenceInMillis / 60000;
            record.put("time", differenceInMinutes);
            data.put("record"+count, record);
        }
        data.put("count",count);
        root.put("data", data);
        return root;
    }

    private JSONObject processUpdateUserInformation(JSONObject dataObject) {
        int userId = dataObject.getInt("userId");
        String name = dataObject.getString("userName");
        String licensePlate = dataObject.getString("licensePlate");
        String image = dataObject.getString("image");
        //先把车牌号存储到审核表
        LicenseReviewDao licenseReviewDao = new LicenseReviewDao();
        LicenseReview licenseReview = new LicenseReview(userId,licensePlate,0,new java.util.Date(System.currentTimeMillis()));
        licenseReviewDao.insertLicenseReview(licenseReview);
        //修改用户信息
        UserDao userDao = new UserDao();
        User user = new User(userId,name,Base64.getDecoder().decode(image));
        boolean b = userDao.updateUserSimple(user);
        JSONObject root = EmptyJSON.getForData();
        JSONObject data = new JSONObject();
        data.put("describe","UpdateUserInformation");
        if(b){
            data.put("reply","ok");
        }else {
            data.put("reply","no");
        }
        root.put("data",data);
        return root;
    }

    private JSONObject processPaySuccess(JSONObject dataObject) {
        ParkingRecordDao parkingRecordDao = new ParkingRecordDao();
        parkingRecordDao.markFirstUnpaidRecordAsPaidByUserId(dataObject.getInt("userId"));
        return null;
    }

    private JSONObject processEndTimingSignal(JSONObject dataObject) {
        int userId = dataObject.getInt("userId");
        int spaceId = dataObject.getInt("spaceId");

        JSONObject root = EmptyJSON.getForCommand();
        JSONObject data = new JSONObject();
        data.put("describe","EndTimingSignal");
        data.put("reply","ok");
        //删除预约
        ReservationDao reservationDao = new ReservationDao();
        List<Reservation> list = reservationDao.getUserReservations(userId);
        Reservation reservation = list.get(0);

        ParkingTransactionService parkingTransactionService = new ParkingTransactionService();
        parkingTransactionService.updateSpaceStatus(userId,spaceId,0);
        //记录数据
        ParkingRecordDao parkingRecordDao = new ParkingRecordDao();
        ParkingRecord parkingRecord = new ParkingRecord(userId,spaceId,
                reservation.getStartTime(),new Date(System.currentTimeMillis()),dataObject.getDouble("money"));
        parkingRecordDao.insertParkingRecord(parkingRecord);

        root.put("data",data);
        WebSocketResourceManager.setTimerTask(true);
        return root;
    }

    private JSONObject processStartTimingSignal(JSONObject dataObject) {
        JSONObject root = EmptyJSON.getForCommand();
        JSONObject data = new JSONObject();

        ReservationDao reservationDao = new ReservationDao();
        reservationDao.updateStartTimeByUserIdAndSpaceId(dataObject.getInt("userId"),
                dataObject.getInt("spaceId"),new Date(System.currentTimeMillis()));
        data.put("describe","StartTimingSignal");
        data.put("reply","ok");

        root.put("data",data);
        return root;
    }

    private JSONObject processQueryUserInformation(JSONObject dataObject) {
        JSONObject command = EmptyJSON.getForCommand();
        JSONObject data = new JSONObject();
        JSONObject user = new JSONObject();
        data.put("describe", "QueryUserInformation");
        UserDao userDao = new UserDao();
        User userById = userDao.getUserById(dataObject.getInt("userId"));
        user.put("name", userById.getName());
        user.put("phone", userById.getPhone());
        user.put("account", userById.getAccount());
        user.put("licensePlate", userById.getLicensePlate());
        user.put("image", Base64.getEncoder().encodeToString(userById.getImage()));
        data.put("user", user);
        command.put("data", data);
        return command;
    }

    private JSONObject processParkingLockControl(JSONObject dataObject) {
        ParkingSpaceDao parkingSpaceDao = new ParkingSpaceDao();
        String lockState = dataObject.getString("lockState");
        JSONObject root = new JSONObject();
        root.put("type","lock");
        int state;
        if(lockState.equals("locked")) {
            state = 1;
            root.put("value",0);
        }else {
            state = 0;
            root.put("value",1);
        }
        socketServer.sendMessage("1",root.toString());
        parkingSpaceDao.updateLockStatus(Integer.parseInt(dataObject.getString("spaceId")),state);
        return null;
    }

    private JSONObject processCancelAppointment(JSONObject dataObject) {
        ParkingTransactionService transactionService = new ParkingTransactionService();
        int i = transactionService.updateSpaceStatus(dataObject.getInt("userId"),
                Integer.parseInt(dataObject.getString("spaceId")), 0);
        WebSocketResourceManager.setTimerTask(true);
        return null;
    }

    private JSONObject processReserveAParkingSpace(JSONObject dataObject) {
        JSONObject root = EmptyJSON.getForData();
        JSONObject data = new JSONObject();
        data.put("describe", "ReserveAParkingSpace");
        data.put("id", dataObject.getString("id"));
        ParkingTransactionService transactionService = new ParkingTransactionService();
        int i = transactionService.updateSpaceStatus(dataObject.getInt("userId"),Integer.parseInt(dataObject.getString("id"))
                , Integer.parseInt(dataObject.getString("state")));
        switch (i) {
            case 0:
                data.put("reply","no");
                break;
            default:
                data.put("reply","yes");
        }
        root.put("data", data);
        WebSocketResourceManager.setTimerTask(true);
        return root;
    }

    private JSONObject processCheckParkingSpace(JSONObject dataObject) {
        JSONObject root = EmptyJSON.getForData();
        JSONObject data = new JSONObject();
        data.put("describe","CheckParkingSpace");
        ParkingSpaceDao parkingSpaceDao = new ParkingSpaceDao();
        List<ParkingSpace> allParkingSpaces = parkingSpaceDao.getAllParkingSpaces();
        JSONObject parkingSpace = new JSONObject();
        for (ParkingSpace space : allParkingSpaces) {
            Integer spaceId = space.getSpaceId();
            Integer status = space.getStatus(); // 0:可用, 1:使用中, 2:停用
            parkingSpace.put(String.valueOf(spaceId),String.valueOf(status));
        }
        data.put("parkingSpace",parkingSpace);
        root.put("data",data);
        return root;
    }

    public JSONObject processHomeRequestData(JSONObject dataObject) {
        JSONObject root = EmptyJSON.getForData();
        JSONObject data = new JSONObject();
        data.put("describe","HomeRequestData");
        ParkingLotDao parkingLotDao = new ParkingLotDao();
        ParkingLot parkingLotById = parkingLotDao.getParkingLotById(1);
        JSONObject parkingLot = new JSONObject();
        parkingLot.put("name",parkingLotById.getName());
        parkingLot.put("totalSpaces",parkingLotById.getTotalSpaces());
        parkingLot.put("availableSpaces",parkingLotById.getAvailableSpaces());
        parkingLot.put("rate1",parkingLotById.getRate1());
        parkingLot.put("rate2",parkingLotById.getRate2());
        data.put("parkingLot",parkingLot);
        if(dataObject!=null){
            JSONObject user = new JSONObject();
            ReservationDao reservationDao = new ReservationDao();
            List<Reservation> list = reservationDao.getUserReservations(dataObject.getInt("userId"));
            if(list.size()>0){
                Reservation reservation = list.get(0);
                user.put("spaceId",reservation.getSpaceId());
                if(reservation.getStartTime() != null){
                    user.put("start",true);
                    user.put("startTime",reservation.getReservationTime().getTime());
                }else{
                    user.put("start",false);
                    user.put("startTime",0);
                }
            }else{
                user.put("spaceId",0);
                user.put("start",false);
                user.put("startTime",0);
            }
            ParkingRecordDao dao = new ParkingRecordDao();
            ParkingRecord unpaidRecord = dao.getFirstUnpaidRecordByUserId(dataObject.getInt("userId"));
            if (unpaidRecord != null) {
                user.put("money",unpaidRecord.getCost());
            } else {
                user.put("money",0.0);
            }
            data.put("user",user);
        }
        root.put("data",data);
        return root;
    }

    private JSONObject processUserRegister(JSONObject dataObject) {
        JSONObject root = EmptyJSON.getForData();
        JSONObject data = new JSONObject();
        data.put("describe","UserRegister");
        String name = dataObject.getString("name");
        String account = dataObject.getString("account");
        String password = dataObject.getString("password");
        String phone = dataObject.getString("phone");
        User user = new User(name, account, password, phone);
        UserDao userDao = new UserDao();
        if(userDao.getUserByAccount(account)!=null) {
            data.put("reply","noAccount");
        }else if(userDao.getUserByPhone(phone)!=null) {
            data.put("reply","noPhone");
        }else if(userDao.insertUser(user)){
            data.put("reply","yes");
        }else{
            data.put("reply","no");
        }
        root.put("data",data);
        return root;
    }

    private JSONObject processUserLoginVerification(JSONObject jsonData) {
        String account = jsonData.getString("account");
        String password = jsonData.getString("password");
        JSONObject root = EmptyJSON.getForData();
        JSONObject data = new JSONObject();
        data.put("describe","UserLoginVerification");
        UserDao userDao = new UserDao();
        User user = userDao.getUserByAccount(account);
        System.out.println(user);
        if(user.getPassword().equals(password)){
            data.put("reply","yes");
            data.put("userId",user.getUserId());
        }else{
            data.put("reply","no");
        }
        root.put("data",data);
        return root;
    }
} 