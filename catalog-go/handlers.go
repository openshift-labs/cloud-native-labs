package main

import (
	"net/http"
	"html/template"
	"log"
	"encoding/json"
)

/**
* Flag for throwing a 503 when enabled
*/
var misbehave = false

func HomePage(w http.ResponseWriter, r *http.Request){

	template := template.Must(template.ParseFiles("templates/homepage.html"))
	  
    err := template.Execute(w, nil) //execute the template
    if err != nil { // if there is an error
		log.Print("template executing error: ", err) //log it
		http.Error(w, err.Error(), http.StatusInternalServerError)
  	}
}

func GetProducts(w http.ResponseWriter, r *http.Request){

	if misbehave {
		w.WriteHeader(http.StatusServiceUnavailable)
		w.Write([]byte("Misbehavior of the Catalog GoLang Service\n"))
	} else {
		products := Products{
			Product{ ItemId: "329299", Name: "Red Fedora", Description: "OFFICIAL RED HAT FEDORA", Price: 34.99},
			Product{ ItemId: "329199", Name: "Forge Laptop Sticker", Description: "JBOSS COMMUNITY FORGE PROJECT STICKER", Price: 8.50},
			Product{ ItemId: "165613", Name: "Solid Performance Polo", Description: "MOISTURE-WICKING, ANTIMICROBIAL 100% POLYESTER DESIGN WICKS FOR LIFE OF GARMENT. NO-CURL, RIB-KNIT COLLAR...", Price: 17.80},
			Product{ ItemId: "165614", Name: "Ogio Caliber Polo", Description: "MOISTURE-WICKING 100% POLYESTER. RIB-KNIT COLLAR AND CUFFS; OGIO JACQUARD TAPE INSITEM_IDE NECK; BAR-TACKED THREE-BUTTON PLACKET WITH...", Price: 28.75},
			Product{ ItemId: "165954", Name: "16 oz. Vortex Tumbler", Description: "DOUBLE-WALL INSULATED, BPA-FREE, ACRYLIC CUP. PUSH-ON LITEM_ID WITH THUMB-SLITEM_IDE CLOSURE; FOR HOT AND COLD BEVERAGES. HOLDS 16 OZ. HAND WASH ONLY. IMPRINT. CLEAR.", Price: 6.00},
			Product{ ItemId: "444434", Name: "Pebble Smart Watch", Description: "SMART GLASSES AND SMART WATCHES ARE PERHAPS TWO OF THE MOST EXCITING DEVELOPMENTS IN RECENT YEARS.", Price: 24.00},
			Product{ ItemId: "444435", Name: "Oculus Rift", Description: "THE WORLD OF GAMING HAS ALSO UNDERGONE SOME VERY UNIQUE AND COMPELLING TECH ADVANCES IN RECENT YEARS. VIRTUAL REALITY...", Price: 106.00},
			Product{ ItemId: "444436", Name: "Lytro Camera", Description: "CONSUMERS WHO WANT TO UP THEIR PHOTOGRAPHY GAME ARE LOOKING AT NEWFANGLED CAMERAS LIKE THE LYTRO FIELD CAMERA, DESIGNED TO ...", Price: 44.30},
		}
		
		// Define Content-Type = application/json
		w.Header().Set("Content-Type", "application/json; charset=UTF-8")
		if err := json.NewEncoder(w).Encode(products); err != nil {
			panic(err)
		}
	}
}

func Behave(w http.ResponseWriter, r *http.Request){
	misbehave = false
	log.Print("'misbehave' has been set to 'false'") 
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("Next request to / will return 200\n"))
	return
}

func Misbehave(w http.ResponseWriter, r *http.Request){
	misbehave = true
	log.Print("'misbehave' has been set to 'true'")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("Next request to / will return a 503\n"))
	return
}