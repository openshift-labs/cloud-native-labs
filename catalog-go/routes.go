package main

import (
	"net/http"
	"github.com/gorilla/mux"
)

type Route struct {
    Name        string
    Method      string
    Pattern     string
    HandlerFunc http.HandlerFunc
}
 
type Routes []Route

func NewRouter() *mux.Router {
	router := mux.NewRouter().StrictSlash(true)
	router.
		PathPrefix("/static/").
		Handler(http.StripPrefix("/static/",
		 	http.FileServer(http.Dir("static"))))

    for _, route := range routes {
        router.
            Methods(route.Method).
            Path(route.Pattern).
            Name(route.Name).
            Handler(route.HandlerFunc)
    }
 
    return router
}

var routes = Routes{
    Route{
        "HomePage",
        "GET",
        "/",
        HomePage,
    },
    Route{
        "GetProducts",
        "GET",
        "/api/catalog",
        GetProducts,
    },
    Route{
        "Misbehave",
        "GET",
        "/api/misbehave",
        Misbehave,
    },
    Route{
        "Behave",
        "GET",
        "/api/behave",
        Behave,
    },
}