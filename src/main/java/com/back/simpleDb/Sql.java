package com.back.simpleDb;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

// SQL만들고 실행하는 클래스
public class Sql {
    private SimpleDb dbConn;
    private String sqlString;
    private List<Object> paramList;

    public Sql(SimpleDb db) {
        this.dbConn = db;
        this.sqlString = "";
        this.paramList = new ArrayList<Object>();
    }

    // SQL조각 추가하기
    public Sql append(String sqlPart, Object... values) {
        // SQL이 있으면 줄바꿈 넣기
        if (this.sqlString.length() > 0) {
            this.sqlString = this.sqlString + "\n";
        }

        // SQL추가
        this.sqlString = this.sqlString + sqlPart.trim();

        // 파라미터 추가
        if (values != null) {
            for (int i = 0; i < values.length; i = i + 1) {
                this.paramList.add(values[i]);
            }
        }

        return this;
    }


    public Sql appendIn(String sqlPart, Object... values) {
        // 값이 없으면 NULL넣기
        if (values == null || values.length == 0) {
            String newSql = sqlPart.replace("?", "NULL");
            return this.append(newSql);
        }

        // 배열인지 체크
        if (values.length == 1 && values[0] != null) {
            if (values[0].getClass().isArray() == true) {
                Object[] arr = (Object[]) values[0];
                values = arr;
            }
        }

        // ?를 여러개로 만들기
        String questionMarks = "";
        for (int i = 0; i < values.length; i = i + 1) {
            if (i > 0) {
                questionMarks = questionMarks + ", ";
            }
            questionMarks = questionMarks + "?";
        }

        // SQL에서 ? 바꾸기
        String newSql = sqlPart.replace("?", questionMarks);

        // SQL추가
        if (this.sqlString.length() > 0) {
            this.sqlString = this.sqlString + "\n";
        }
        this.sqlString = this.sqlString + newSql;

        // 파라미터 추가
        for (int i = 0; i < values.length; i = i + 1) {
            this.paramList.add(values[i]);
        }

        return this;
    }

    // INSERT실행
    public long insert() {
        long result = this.dbConn.executeInsert(this.sqlString, this.paramList);
        return result;
    }

    // UPDATE실행
    public int update() {
        int result = this.dbConn.executeUpdate(this.sqlString, this.paramList);
        return result;
    }

    // DELETE실행
    public int delete() {
        int result = this.dbConn.executeUpdate(this.sqlString, this.paramList);
        return result;
    }

    // 여러행 가져오기
    public List<Map<String, Object>> selectRows() {
        List<Map<String, Object>> result = this.dbConn.executeSelect(this.sqlString, this.paramList);
        return result;
    }

    // 한행 가져오기
    public Map<String, Object> selectRow() {
        List<Map<String, Object>> rows = this.selectRows();
        if (rows.size() == 0) {
            Map<String, Object> emptyMap = new HashMap<String, Object>();
            return emptyMap;
        }
        Map<String, Object> firstRow = rows.get(0);
        return firstRow;
    }

    // 문자열 하나 가져오기
    public String selectString() {
        Map<String, Object> row = this.selectRow();
        if (row.size() == 0) {
            return null;
        }

        // 첫번째값 찾기
        Object firstValue = null;
        for (String key : row.keySet()) {
            firstValue = row.get(key);
            break;
        }

        if (firstValue == null) {
            return null;
        }
        String result = firstValue.toString();
        return result;
    }

    // 숫자 하나 가져오기
    public Long selectLong() {
        Map<String, Object> row = this.selectRow();
        if (row.size() == 0) {
            return null;
        }

        // 첫번째값 찾기
        Object firstValue = null;
        for (String key : row.keySet()) {
            firstValue = row.get(key);
            break;
        }

        if (firstValue == null) {
            return null;
        }

        Long result = null;
        if (firstValue instanceof Number) {
            Number num = (Number) firstValue;
            result = num.longValue();
        } else {
            String str = firstValue.toString();
            result = Long.parseLong(str);
        }

        return result;
    }

    // boolean 하나 가져오기
    public Boolean selectBoolean() {
        Map<String, Object> row = this.selectRow();
        if (row.size() == 0) {
            return null;
        }

        // 첫번째값 찾기
        Object firstValue = null;
        for (String key : row.keySet()) {
            firstValue = row.get(key);
            break;
        }

        if (firstValue == null) {
            return null;
        }

        Boolean result = null;
        if (firstValue instanceof Boolean) {
            result = (Boolean) firstValue;
        } else if (firstValue instanceof Number) {
            Number num = (Number) firstValue;
            if (num.intValue() == 0) {
                result = false;
            } else {
                result = true;
            }
        } else if (firstValue instanceof byte[]) {
            byte[] bytes = (byte[]) firstValue;
            if (bytes.length == 1) {
                if (bytes[0] == 0) {
                    result = false;
                } else {
                    result = true;
                }
            }
        } else {
            String str = firstValue.toString();
            result = Boolean.parseBoolean(str);
        }

        return result;
    }

    // 날짜 하나 가져오기
    public LocalDateTime selectDatetime() {
        Map<String, Object> row = this.selectRow();
        if (row.size() == 0) {
            return null;
        }

        // 첫번째값 찾기
        Object firstValue = null;
        for (String key : row.keySet()) {
            firstValue = row.get(key);
            break;
        }

        LocalDateTime result = null;
        if (firstValue instanceof LocalDateTime) {
            result = (LocalDateTime) firstValue;
        }

        return result;
    }

    // 숫자 여러개 가져오기
    public List<Long> selectLongs() {
        List<Map<String, Object>> rows = this.selectRows();
        List<Long> resultList = new ArrayList<Long>();

        for (int i = 0; i < rows.size(); i = i + 1) {
            Map<String, Object> row = rows.get(i);

            // 첫번째값 찾기
            Object firstValue = null;
            for (String key : row.keySet()) {
                firstValue = row.get(key);
                break;
            }

            if (firstValue != null) {
                Long longValue = null;
                if (firstValue instanceof Number) {
                    Number num = (Number) firstValue;
                    longValue = num.longValue();
                } else {
                    String str = firstValue.toString();
                    longValue = Long.parseLong(str);
                }
                resultList.add(longValue);
            }
        }

        return resultList;
    }

    // 객체리스트로 변환
    public <T> List<T> selectRows(Class<T> typeClass) {
        List<Map<String, Object>> rows = this.selectRows();
        List<T> resultList = new ArrayList<T>();

        for (int i = 0; i < rows.size(); i = i + 1) {
            Map<String, Object> row = rows.get(i);
            T obj = this.mapToObject(typeClass, row);
            resultList.add(obj);
        }

        return resultList;
    }

    // 객체 하나로 변환
    public <T> T selectRow(Class<T> typeClass) {
        Map<String, Object> row = this.selectRow();
        if (row.size() == 0) {
            return null;
        }

        T result = this.mapToObject(typeClass, row);
        return result;
    }

    // Map을 객체로 바꾸기
    private <T> T mapToObject(Class<T> typeClass, Map<String, Object> rowMap) {
        T obj = null;

        try {
            // 객체 만들기
            obj = typeClass.getDeclaredConstructor().newInstance();

            // Map의 모든 항목 처리
            for (String columnName : rowMap.keySet()) {
                Object value = rowMap.get(columnName);

                // 필드이름 정하기
                String fieldName = columnName;
                if (columnName.equals("isBlind") == true) {
                    fieldName = "blind";
                }

                // 필드 찾기
                Field field = null;
                try {
                    field = typeClass.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    continue; // 필드 없으면 넘어가기
                }

                field.setAccessible(true);

                // 값 넣기
                if (value == null) {
                    field.set(obj, null);
                } else {
                    Class<?> fieldType = field.getType();

                    if (fieldType == boolean.class || fieldType == Boolean.class) {
                        if (value instanceof Boolean) {
                            field.set(obj, value);
                        } else if (value instanceof Number) {
                            Number num = (Number) value;
                            if (num.intValue() == 0) {
                                field.set(obj, false);
                            } else {
                                field.set(obj, true);
                            }
                        } else if (value instanceof byte[]) {
                            byte[] bytes = (byte[]) value;
                            if (bytes.length == 1) {
                                if (bytes[0] == 0) {
                                    field.set(obj, false);
                                } else {
                                    field.set(obj, true);
                                }
                            }
                        } else {
                            String str = value.toString();
                            Boolean boolValue = Boolean.parseBoolean(str);
                            field.set(obj, boolValue);
                        }
                    }
                    else if (fieldType == long.class || fieldType == Long.class) {
                        if (value instanceof Number) {
                            Number num = (Number) value;
                            Long longValue = num.longValue();
                            field.set(obj, longValue);
                        } else {
                            String str = value.toString();
                            Long longValue = Long.parseLong(str);
                            field.set(obj, longValue);
                        }
                    }
                    else if (fieldType == int.class || fieldType == Integer.class) {
                        if (value instanceof Number) {
                            Number num = (Number) value;
                            Integer intValue = num.intValue();
                            field.set(obj, intValue);
                        } else {
                            String str = value.toString();
                            Integer intValue = Integer.parseInt(str);
                            field.set(obj, intValue);
                        }
                    }
                    else if (fieldType == String.class) {
                        String strValue = value.toString();
                        field.set(obj, strValue);
                    }
                    else if (fieldType == LocalDateTime.class) {
                        if (value instanceof LocalDateTime) {
                            field.set(obj, value);
                        }
                    }
                    else {
                        field.set(obj, value);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("객체변환 에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return obj;
    }
}