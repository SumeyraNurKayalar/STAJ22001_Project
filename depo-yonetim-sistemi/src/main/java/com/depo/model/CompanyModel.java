package com.depo.model;

/**
 * Tedarikçi ve Firma verilerini temsil eden model sınıfı.
 */
public class CompanyModel {

    private int id;
    private String companyName;
    private String contactPerson;
    private String phone;
    private String email;
    private String taxNumber;
    private String description;

    // Parametresiz Constructor
    public CompanyModel() {
    }

    // 7 Parametreli Constructor
    public CompanyModel(int id, String companyName, String contactPerson, String phone, String email, String taxNumber, String description) {
        this.id = id;
        this.companyName = companyName;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.taxNumber = taxNumber;
        this.description = description;
    }

    public CompanyModel(int id, String companyName, String contactPerson, String phone, String email, String taxNumber) {
        this(id, companyName, contactPerson, phone, email, taxNumber, "-");
    }

    public int getId() { return id; }
    public String getCompanyName() { return companyName; }
    public String getContactPerson() { return contactPerson; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public String getTaxNumber() { 
        return (taxNumber != null && !taxNumber.trim().isEmpty()) ? taxNumber : "-"; 
    }

    public String getDescription() { 
        return (description != null && !description.trim().isEmpty()) ? description : "-"; 
    }

    public String getTaxInfo() { return getTaxNumber(); }

    public void setId(int id) { this.id = id; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
    public void setDescription(String description) { this.description = description; }
    public void setTaxInfo(String taxInfo) { setTaxNumber(taxInfo); }

    @Override
    public String toString() {
        return companyName != null ? companyName : "";
    }
}