package com.diagonal.cordeis.model;

public enum Permission {
    // Books permissions
    MANAGE_BOOKS,    // Admin only - create, update, delete
    VIEW_BOOKS,      // Everyone - list books
    VIEW_BOOK_DETAILS, // User+ - view detailed book info

    // Collections permissions
    MANAGE_COLLECTIONS, // Admin only - create, update, delete
    VIEW_COLLECTIONS,   // User+ - view collections

    // Import permissions
    IMPORT_BOOKS,     // Admin only - import books

    // Printing permissions (available to all)
    VIEW_PRINTERS,    // Everyone - see printers
    PRINT_BOOKS,      // Everyone - print books

    // User management permissions
    MANAGE_USERS      // Admin only - user administration
}
