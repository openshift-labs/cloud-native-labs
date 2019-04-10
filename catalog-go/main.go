package main

import (
	"log"
	"net/http"
)

func main() {
	
	router := NewRouter()

	log.Println("Listening...")
	log.Fatal(http.ListenAndServe(":8080", router))
}