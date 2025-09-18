package com.back.simpleDb;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// DB연결하고 SQL실행
public class SimpleDb {
    private String host;
    private String username;
    private String password;
    private String dbName;

    private boolean devMode = false;

    // 연결 저장하는곳
    private static ThreadLocal<Connection> connStorage = new ThreadLocal<Connection>();

    public SimpleDb(String host, String username, String password, String dbName) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.dbName = dbName;

        // 드라이버 로드
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 실패");
            throw new RuntimeException(e);
        }
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    // DB연결 가져오기
    private Connection getConn() throws SQLException {
        Connection conn = connStorage.get();
        if (conn == null || conn.isClosed()) {
            String url = "jdbc:mysql://" + this.host + ":3306/" + this.dbName + "?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
            conn = DriverManager.getConnection(url, this.username, this.password);
            conn.setAutoCommit(true);
            connStorage.set(conn);
        }
        return conn;
    }

    // SQL실행하기
    public void run(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConn();
            ps = conn.prepareStatement(sql);

            // 파라미터 넣기
            if (params != null) {
                for (int i = 0; i < params.length; i = i + 1) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            if (devMode == true) {
                System.out.println("실행SQL: " + sql);
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("SQL실행 에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Sql객체 만들기
    public Sql genSql() {
        Sql sqlObj = new Sql(this);
        return sqlObj;
    }

    // INSERT실행하고 ID가져오기
    long executeInsert(String sql, List<Object> params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        long result = 0;

        try {
            conn = getConn();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // 파라미터 넣기
            if (params != null) {
                for (int i = 0; i < params.size(); i = i + 1) {
                    Object param = params.get(i);
                    ps.setObject(i + 1, param);
                }
            }

            if (devMode == true) {
                System.out.println("실행SQL: " + sql);
            }

            ps.executeUpdate();

            // 생성된키 가져오기
            rs = ps.getGeneratedKeys();
            if (rs.next() == true) {
                result = rs.getLong(1);
            }

        } catch (SQLException e) {
            System.out.println("INSERT 에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    // UPDATE, DELETE실행
    int executeUpdate(String sql, List<Object> params) {
        Connection conn = null;
        PreparedStatement ps = null;
        int result = 0;

        try {
            conn = getConn();
            ps = conn.prepareStatement(sql);

            // 파라미터 넣기
            if (params != null) {
                for (int i = 0; i < params.size(); i = i + 1) {
                    Object param = params.get(i);
                    ps.setObject(i + 1, param);
                }
            }

            if (devMode == true) {
                System.out.println("실행SQL: " + sql);
            }

            result = ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("UPDATE/DELETE 에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    // SELECT실행
    List<Map<String, Object>> executeSelect(String sql, List<Object> params) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        try {
            conn = getConn();
            ps = conn.prepareStatement(sql);

            // 파라미터 넣기
            if (params != null) {
                for (int i = 0; i < params.size(); i = i + 1) {
                    Object param = params.get(i);
                    ps.setObject(i + 1, param);
                }
            }

            if (devMode == true) {
                System.out.println("실행SQL: " + sql);
            }

            rs = ps.executeQuery();

            // 컬럼정보 가져오기
            ResultSetMetaData metaData = rs.getMetaData();
            int colCount = metaData.getColumnCount();

            // 결과처리
            while (rs.next() == true) {
                Map<String, Object> rowMap = new HashMap<String, Object>();

                for (int i = 1; i <= colCount; i = i + 1) {
                    String colName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);

                    // 타입변환
                    if (value != null) {
                        // DATETIME변환
                        if (value instanceof Timestamp) {
                            Timestamp ts = (Timestamp) value;
                            value = ts.toLocalDateTime();
                        }
                        // BIT변환
                        if (value instanceof byte[]) {
                            byte[] bytes = (byte[]) value;
                            if (bytes.length == 1) {
                                if (bytes[0] == 0) {
                                    value = false;
                                } else {
                                    value = true;
                                }
                            }
                        }
                    }

                    rowMap.put(colName, value);
                }
                resultList.add(rowMap);
            }

        } catch (SQLException e) {
            System.out.println("SELECT 에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultList;
    }

    // 트랜잭션 시작
    public void startTransaction() {
        Connection conn = null;
        try {
            conn = getConn();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            System.out.println("트랜잭션 시작 에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // 커밋
    public void commit() {
        Connection conn = null;
        try {
            conn = getConn();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("커밋 에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // 롤백
    public void rollback() {
        Connection conn = null;
        try {
            conn = getConn();
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("롤백 에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // 연결닫기
    public void close() {
        Connection conn = connStorage.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("연결닫기 에러: " + e.getMessage());
                e.printStackTrace();
            }
            connStorage.remove();
        }
    }
}