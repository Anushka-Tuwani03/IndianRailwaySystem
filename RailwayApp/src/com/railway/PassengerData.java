package com.railway;

public class PassengerData {
    public String passenger_id;
    public String name;
    public int age;
    public String gender;
    public String id_proof;
    public String email;
    public String seatNo;
    public String status;
    public boolean valid;

    public PassengerData(String passenger_id, String name, int age, String gender, String id_proof,
                     String email, String seatNo, String status, boolean valid) {
        this.passenger_id = passenger_id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.id_proof = id_proof;
        this.email = email;
        this.seatNo = seatNo;
        this.status = status;
        this.valid = valid;
    }
}
