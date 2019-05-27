
.PHONY: compile-local
compile-local:
	CGO_ENABLED=0 go build -gcflags "-N -l" -ldflags="-compressdwarf=false" -o catalog-go.out .
