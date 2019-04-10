package main

type Product struct {
    ItemId 		string	`json:"itemId"`
    Name 		string  `json:"name"`
	Description string  `json:"description"`
	Price       float32 `json:"price"`
}

type Products []Product