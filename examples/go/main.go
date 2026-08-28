// Golden Path Go hello + About + crash sanitize (no network, no PII).
package main

import "fmt"

func main() {
	fmt.Println(Greet())
	fmt.Println(AboutSummary())
}
