package carsharing;

import java.util.ArrayList;
import java.util.Scanner;

public class MenuManager {
    enum Status {LOG_IN, MAIN_MENU, CREATE_COMPANY, CAR_MENU, CREATE_CAR, CUSTOMER_MENU, CREATE_CUSTOMER, RENTALS }

    private static final Scanner in = new Scanner(System.in);
    private static Status status;
    private static int companyID;
    private static int customerID;
    private static int carID;
    private static ArrayList<Company> companies;
    private static ArrayList<Car> cars;
    private static ArrayList<Customer> customers;


    public static void start() {
        status = Status.LOG_IN;
        printMenu();
    }

    private static void printMenu() {
        switch (status) {
            case LOG_IN:
                LogIn();
                break;
            case MAIN_MENU:
                mainMenu();
                break;
            case CREATE_COMPANY:
                createCompany();
                break;
            case CAR_MENU:
                carMenu();
                break;
            case CREATE_CAR:
                createCar();
                break;
            case CUSTOMER_MENU:
                customerMenu();
                break;
            case CREATE_CUSTOMER:
                createCustomer();
                break;
            case RENTALS:
                carRentalMenu();
                break;
        }
    }

    private static void carRentalMenu() {
        System.out.println("1. Rent a car\n" +
                "2. Return a rented car\n" +
                "3. My rented car\n" +
                "0. Back");
        switch (input()) {
            case 1:
                rentCar();
                break;
            case 2:
                returnRental();
                break;
            case 3:
                rentalStatus();
                break;
            case 0:
                status = Status.LOG_IN;
                break;
        }
        printMenu();
    }

    private static void rentalStatus() {
        if(customers.get(customerID-1).getCarID() == 0) {
            System.out.println("You didn't rent a car!");
        } else {
            System.out.println("Your rented car :\n"
                    + cars.get(customers.get(customerID-1).getCarID()-1).getName() +
                    "\nCompany:\n" + companies.get(companyID-1).getName());
            System.out.println();
        }
    }

    private static void returnRental() {
        if(customers.get(customerID-1).getCarID() == 0) {
            System.out.println("You didn't rent a car!");
        } else {
            customers.get(customerID-1).setCarID(0);
            System.out.println("You've returned a rented car!");
            H2Dao.updateCustomer(customerID);
        }
    }

    private static void rentCar() {
        companies = H2Dao.getAllCompanies();
        if (customers.get(customerID-1).getCarID() > 0) {
            System.out.println("You've already rented a car!");
            status = Status.RENTALS;
        } else{
            System.out.println("Choose the company:");
            for (Company company : companies) {
                System.out.println(company.getId() + ". " + company.getName());
            }
            System.out.println("0. Back");
            int option = input();
            if(option == 0) {
                status = Status.RENTALS;
            } else {
                System.out.println("'" + companies.get(option-1).getName() + "' company");
                companyID = option;
                cars = H2Dao.getAllCars(companyID);
                if (cars.isEmpty()) {
                    System.out.println("The car list is empty!");
                } else {
                    for (int i = 0; i < customers.size(); i++) {
                        if(customers.get(i).getCarID() > 0) {
                            cars.remove(customers.get(i).getCarID()-1);
                        }
                    }
                    for (int i = 0; i < cars.size(); i++) {
                        System.out.println((i + 1) + ". " + cars.get(i).getName());
                    }
                    System.out.println("0. Back");
                    option = input();
                    if (option == 0) {
                        status = Status.LOG_IN;
                    } else {
                        carID = option;
                        System.out.println("You rented '" + cars.get(option-1).getName() + "' ");
                        H2Dao.updateCustomer(carID, customerID);
                        customers.get(customerID-1).setCarID(carID);
                        status = Status.RENTALS;
                    }
                }
            }
        }
        printMenu();
    }


    private static void customerMenu() {
        customers = H2Dao.getAllCustomers();
        if (customers.isEmpty()) {
            System.out.println("The customer list is empty!");
            status = Status.LOG_IN;
        } else{
            System.out.println("Choose the customer:");
            for (Customer customer : customers) {
                System.out.println(customer.getId() + ". " + customer.getName());
            }
            System.out.println("0. Back");
            int option = input();
            if(option == 0) {
                status = Status.MAIN_MENU;
            } else {
                System.out.println("'" + customers.get(option-1).getName() + "' customer");
                customerID = option;
                status = Status.RENTALS;
            }
        }
        printMenu();
    }

    private static void createCustomer() {
        System.out.println("Enter the customer name:");
        H2Dao.createCustomer(in.nextLine());
        System.out.println("The customer was created!");
        status = Status.LOG_IN;
        printMenu();
    }

    private static void LogIn() {
        System.out.println("1. Log in as a manager\n" +
                "2. Log in as a customer\n" +
                "3. Create a customer\n" +
                "0. Exit");
        switch (input()) {
            case 1:
                status = Status.MAIN_MENU;
                break;
            case 2:
                status = Status.CUSTOMER_MENU;
                break;
            case 3:
                status = Status.CREATE_CUSTOMER;
                break;
            case 0:
                return;
        }
        printMenu();
    }

    private static void createCompany() {
        System.out.println("Enter the company name:");
        H2Dao.createCompany(in.nextLine());
        System.out.println("The company was created!");
        status = Status.MAIN_MENU;
        printMenu();
    }

    private static void createCar(){
        System.out.println("Enter the car name:");
        H2Dao.createCar(in.nextLine(), companyID);
        System.out.println("The car was created!");
        status = Status.CAR_MENU;
        printMenu();
    }

    private static void carMenu() {
        System.out.println("1. Car list\n" +
                "2. Create a car\n" +
                "0. Back"
        );
        switch (input()) {
            case 1:
                printCars();
                break;
            case 2:
                status = Status.CREATE_CAR;
                break;
            case 0:
                status = Status.MAIN_MENU;
                break;
        }
        printMenu();
    }

    private static void mainMenu() {
        System.out.println(
                "1. Company list\n" +
                        "2. Create a company\n" +
                        "0. Back"
        );
        switch (input()) {
            case 1:
                printCompanies();
                break;
            case 2:
                status = Status.CREATE_COMPANY;
                break;
            case 0:
                status = Status.LOG_IN;
                break;
        }
        printMenu();
    }

    private static void printCompanies() {
        companies = H2Dao.getAllCompanies();
        if (companies.isEmpty()) {
            System.out.println("The company list is empty!");
            status = Status.MAIN_MENU;
        } else{
            System.out.println("Choose the company:");
            for (int i = 0; i < companies.size(); i++) {
                System.out.println((i + 1) + ". " + companies.get(i).getName());
            }
            System.out.println("0. Back");
            int option = input();
            if(option == 0) {
                status = Status.MAIN_MENU;
            } else {
                System.out.println("'" + companies.get(option-1).getName() + "' company");
                companyID = option;
                status = Status.CAR_MENU;
            }
        }
        printMenu();
    }

    private static void printCars() {
        cars = H2Dao.getAllCars(companyID);
        if (cars.isEmpty()) {
            System.out.println("The car list is empty!");
        } else {
            for (int i = 0; i < cars.size(); i++) {
                System.out.println((i + 1) + ". " + cars.get(i).getName());
            }
            status = Status.CAR_MENU;
        }
        printMenu();
    }

    private static int input() {
        return Integer.parseInt(in.nextLine());
    }
}