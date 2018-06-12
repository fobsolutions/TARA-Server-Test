# Pangalingiga autentimise mock teenus

## Kuidas toimib?

Mock teenus ootab `/ipizza/auth` otspunktil GET ja POST päringuid ning moodustab vastuseks vastavalt päringus leitud tagasipöördumisurlile (`VK_RETURN`) automaatse ümbersuunamisvormi koos autentimisvastusega.

Teenus lisab algses päringus tulnud `VK_SND_ID`, `VK_REC_ID`, `VK_NONCE`, `VK_RID`, `VK_ENCODING` ja `VK_LANG` väärtused vastusesse. Mocki vastuses kasutatavad muud väljad tagastatakse vastavalt seadistusele. Vaikimisi, kui panga jaoks pole tagastatavaid väärtuseid paika pandud (vt allpool toodud seadistamise näiteid), tagastatakse iga panga vastuses väärtused vastavalt `application.properties` failis toodule.


## Quickstart TARA näitel

1. Seadista pangalinki kasutav rakendus vastu mock teenuse otspunkti. Näiteks `http://localhost:8990/ipizza/auth`

2. Genereeri testpanga jaoks RSA võtmepaar.

3. Seadista mock teenus kasutama testpanga puhul punktis 2 genereeritud privaatvõtit, saates HTTP PUT päring otspunktile: `/ipizza/banks?id={panga kood päringu väljas VK_REC_ID}}&key={pkcs8 testvõti base64 kodeeritud kujul}`. <br><br>Näiteks: `curl -X PUT "http://localhost:8990/ipizza/banks?id=SWED&key=MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDFekQLsbFpgq6aLcxTAx2%2FutiQHPQoR8qVm5hDgSd6JqVMHQxop3H%2BhUaHvzIE0a1UB%2B4iQst7iJLhARSBYgAvZ9v04po7i5gbVBjm%2B212ZYPJiVBMhl8o%2Br3EoEFEpkT%2FIrvVkSpvaTeqGvSmIjB8L4ykQMQA%2B4K5bpT9xpOtCVHGkA8X3NDnDWwI1PAx2RKWvhbCVbj%2FRjzcxzEoo0JXrjj9dBHJZabslBlZSNvwb45ZtAgDppom%2F88EWPIcm82JXYQGwzHjETZhlhd9Jj6AcstmFK6xJYRYZZPL0TrWN8FzrKipF5jIwUQ%2Fj6nB82gVlTYWF6yaAoD1eHQWYDAgMBAAECggEARa%2FSyWmoWLp0ERZuTzD%2FU4SFHb4J7xj%2BhXMF9XRjUPjIRibORZxt6TOB4hcY%2FooYm24EI%2F%2BlUEuAAgk1PZCuGUvkMk%2BDRXtfUkYIaDZ0%2BFhR4%2BxvJ4LEbnpraCsc4W9Q31yPTUS8KOkSm3%2Fd2k7gW0KQV%2Fo%2B8LXQuM0sKK3lryf86uVXgt%2ByO9nqYbPDi0y1cx2BvZ4nTnVKyR0sA2lzLyR6vX%2Bi7658VxuZhzSqMdv%2F83tio9VSriAsyiFfA8tx45Z01QOzBteVexmvwD1zvS1Axo0xfT%2BFwDb3ekh2%2FVhjtkweWOhYspRF%2FcP3SQCmAcx19QL2KpS4f8gSqQKBgQD4KqqMvjypvNJhCQ32gzWPwaBZSIqwzM0uSy%2BhRmTDUXufp5DRoz5HILMlKYy3JaD6Fc0VHcx6kGahkBQb1aTvhyfam13l9gYhXI07OspzNuot1HcoBiIdVWNdNMdGddcd6YJ9HxKzrpHJrFC4vhKzTNRS1B476%2BKHEP17AIMgZwKBgQDLtgB8pztv8vLDbpfV9w0odaRElTWEB3aFEKXI2d5CQOaXqGshTY1LXmzuXSF8hp8g%2FWeO7BIMtZ6cUj3NE%2Fp%2FLPNk0%2FsDf3hO8KSboUzzGnF2lPhgUyEUjoiHfoIeIrEyLFRyNKKUKgMQ7LzxsmAqvOwhf30IiY6Zv%2Bl5WcBQKBgQCZdlD3LeD3mfOC3AWNboAdwL%2F21lwljtBHE2mF0rEW83l%2BNjHg4ZDujTMbBQGmdBeC7x5eM4oyNL%2ByoxU8TTZshOjJT5CWVzhdQw13FhkBfHD%2BH%2FMSPBff8vMScV1GNNdQwjcaawBIDU9MEKLUgFJrqZ6eA0b98qZQBLIvrzewKBgCPAh4lZGFdnfi%2FKgx9sQoTtUK0wurm3TZe%2FpZv57mjdl25oGLb3Hi2H93zQpwkeX3AoqZrskoqWpNnc61qF8LuJUFYJo7kfxlLMo99cXSo1yvMWascoMFU%2FL1qQSOsWt0j0DNAu0gS8BRN0SLKAYLVlFp4s9mPp129u4I%2FoLru5AoGAXP8D0fIbul7A%2Fcy%2Fx4hBwx1fRw1DUy8k0xPqkJA2pkn%2F9elKxflMS2aAJK9JUHK0%2FwREwfQuncBBrOv4xHckp1VX5Dp9e5710zou5PfPt4CIrly6RyZYVPb%2FFR4RplPL8pe82%2Fzn6W%2BLZnkv79xrtdeEt%2B0ub3c%2FbjHIDciPM%3D"`

## Otspunktid

`/ipizza/auth` - pangalingi url

`/ipizza/defaults` - vaikeseadete seadistamine

`/ipizza/banks` - pankade haldamine

`/ipizza/banks/{panga identifikaator}/defaults` - panga vastuse väljade seadistamine

`/ipizza/banks/{panga identifikaator}/method` - panga vastuse meetodi seadistus


## Globaalsete vaikeseadete haldamise otspunkt: /ipizza/defaults


PUT - määra parameetrite vaikeväärtusi, näiteks:

PUT http://localhost:8990/ipizza/defaults?VK_USER_NAME=Kasutaja%20Nimi
muudab/määrab VK_USER_NAME parameetri vaikeväärtuseks "Kasutaja Nimi"

curl -X PUT "http://localhost:8990/ipizza/banks?id=COOP&key=MIIJQwIBADANBgkqhkiG9w0BAQEFAASCCS0wggkpAgEAAoICAQCHBd9QV3BgygR6uhnH29P68U5IlgL7AQyA3%2FZ3yIzhQN1P38Be%2BR9BKHObt%2F0g3uSim%2BhPXoEJdJdZU6kLK6VNO7cA4ncJVkwsegQZk8bBQvlq8R00%2BeWqDvEON7oDZEiUsciyTg%2Bp5ode5G1COL7%2F9WT8q3KkkZnfSRWFEE2EkH7FieXaS%2BftaXpBzqer4VdzyFe5lf3daHL%2Fg91l0Ye85pN24vseIJcpHxrmWhN%2BELYDHoug80vqDZy613UcUZ3%2FS2xPHrK9gAO7TigLVaL6IX8leAuxkqN62fhsIe3Evf%2FlbIAp6kJ0Wxr5Ff1PEcuPDbnXKYJkOJCgs6TBj6nXqVkauNqH524hZ2Y5UUfqdyOJbIgpDXwvDIulbTLRdaBk238BYdrdLWk7LCjiqvQH6VsRnXpuhPwHwnBi%2Fu%2BVFzc5aNeRCrdstBWTO5aho%2BdtVOo1PWwZ5i0JwAzIGwTnfuYlCyHfv%2FuzOOTKKYDDNmHFS3XZ8E%2FIujkmzsfxkn9HdTWGayNgVFlvb30EpCtIQoI14maEhTV36iLoBUwA5PsQxMg0djCOwauKCmdZpOgHYLcRlp%2FHacnQbKo2wo2%2FhABfr0adM6V1n9j7oKKUQamaK7s96qjcYTz5pzlEzVQqupguaxhBmErVLjpi4y40RzYVgo1ULOctRpbyHYywSQIDAQABAoICAQCDfmSMiZsXp6XVH4gb6yn2KSpj5jPg3pSerX4KCXWpqNHfB7kAxPx7KDnuXLBXYrqm4c7L%2FO0NmE4M4DywdED3hkAizGnA3how9mMnxZK6g88DJu2augPAKez4O60bwH4u0TTy8LKwc6FB%2BJOu%2BjSIcSN4Lj%2FnpSSFH1elzsqvX0kWd8UxYtrrit1VWTxRV5MjXuiQBFLHXQEhdfoXPodCoArcrRCBtpt%2FoJtB2ldBXmTjhMvWYxp7ReZsz4dtnM6R4ZIJO4w2oIUB1IuK%2FktJ7XU1uEMrG%2FO5oK0eIay8%2FYxIkfH%2B3%2FT3%2FAG4soGOj3X%2BSw%2FBStuBXtT6q%2Fi3yIFN61QWoWSGbgHEYHc6RneT9PwcVHvxdbXBNNOSUD7FQs%2FCfiHQQVadoDBlzxLXwOg1OA52Uglj9kTkKZtHVWEUP69JZSElwP0BfkvvPH9Zx24iE17FkkcjMHHbgR47Fq%2FEcg39VUU03ScaYFZLxySbj%2BJzhzXpmM7icdCzLrPGpV9oSaIR2WaeFW3YHQ3guF2eBIk9FvuZv2wSb8yAglgnlGZs%2Fvlhd73%2B3QCvu1%2Bs%2FEmUZNKEXJvr4%2FpxuW7pxf1ntAEnY4EDD3SW4tRNh%2FhXDzEnK2OT6CzndrpgH56Lq%2BywrpK58JwOFUNYA%2BWqUIj9pLXA2ISOvLCVCL5oVGciQQKCAQEAwlQE9hByvtsrGYigZr7h%2B9Rbkvu88eTPXlTMA5fnWH8pkJnefuEflt6AlK0joC8HB3bDh%2B58EOCPSHe9j6lYseRovwsiugGUlNXX796hOsrDVShhez0IpoxAONWVr%2BaW%2FsvOy63okKn5TmHkJdEbJ36aJASq2FYAHQxxf6DapDwZRG2Jr2ZNUKn9nMnheStAV9hX8F4cnQ%2B0H%2BgvpOyEV5y%2FKKkT49R8TY2SvrJoGOWnRrVt%2F5w7t%2F8CuO%2FMUROGmgrFfm2nlBeHY8hIzOQHeAFhrGTaB5tSElOGvO6F0AOMKf3UHj7ZMlBWEGE2D%2FV0YeRnaYVuVMO4nRjkW3kj%2FwKCAQEAsd%2BpiZA6b1PuZsih%2BWqzNHVLbsozThG7BoB%2Bx676yxeVeA7bgn2gnf6LGm7RxD1fu%2FLhLLctj7tbSDya%2BKR4%2Ba9zE4TlCIAux3dBEebC1pC4oBEMnVwY61gXbS9h9Bd3k5pOqXZdFm9Yd2BCPwxAlm%2BjIBaQQxF72ygRRsgmrnJcClyzyGlbrZOqgueDqhOD1TMR3QMj9sGkXCvED6scXZCgp1OvndlySgTi1tgDuZzIAn12ejKNjldkLiyjF2i2Hde27FZl7iPSScEN2CYiezNUbZTmoadfbH1pkcxF6SFFUHoaHD5c119klSlnIvrTm3A0rswOZhmAXnTO2pgLtwKCAQAjSFP6RTRAONTTdv4AianIKl9SCYjdtnh8HRNJl7v2Umg%2Bi%2BFc0g0FPHLGC8WLIN662TJZLNCz08HQG4V8XvfunJT0EtaERSMv%2Fr%2BNGrPVU9kxTfqdAJDghFhouUbLQCesLV7nljTCh0nvlFBzGUs%2F2XmtDnfifYLZw1ecH51y8kJ4LRcxlHZ3LdYRMIpSkKrHCE23I684SRCgNmzoGdVRfvEqNcCZvguvnQPGweeqlY%2FQg8JsBXbGSnO5FjjXg58NuuTutoQVnEwFruig4soEXOPaBBmeBfJZx3aQORJsmShKu35W23xd6T4nDS9eqGcwGuM3wnUsyLhtNYbIoO8LAoIBAQCO%2B%2BZDCMeQen2n7hwWbSlFQAy1MKFxXltjbKN9TTGZVIn4iqj2Cpq0psuXIgJJp7RvV1yfH1jms4s7VEyXF0%2BQFvOAe1HJNZFlEn4iL8frx0ZClbH4RPJbLOMYCADLwJmE84PMQbOuty78N0rJd8XrqC4PO1QYNdPEZj8NERPXI2LNiZuTvO3PaGam%2BAglDa7qETWI3l9omlzPIvdmSPxyUjJyj5kaYJO44Jigb%2BD%2BsgMtzUESFdUo2SNU4tjvlmLfNmp7LTCe7%2Fi5BRFl8bHCK47wz8NdqzfP9DiK1RRVxUL84Epg7dif4LTwGuonek3kI5yV7r6cYF%2BKjodBvPudAoIBAD%2F%2FoKXGW8jhXcLRSNWkt91IojVTmonl9xfrlU8z%2BXM8XWGpcfTCHdbLInZgRc9JfnAWiOQuFeOBbrlSAZUvgZi49Aj7HWOOitAIPYFZ%2B0u6u493h1RpilBBzX4Xy5w9giRMrAPWXCH9%2Fr3ZGA77qevIGxCIteqVM1NQihgIg4707vTn8mQnXy%2BJSN3puZTukOV3VN41aIy%2BQ%2FeoyGhLqroUkuugL%2BOpbXFpD6Lovp5XRZVvW1iVQOpptHlSYo1dVQ5a79eFXCU9A39e7E0ulpvK89PIalXoWa1mnldbNFjADPpl5IuxkF0U23uOkUCARKnGbFNad8%2FRCo8hh155tRk%3D"

 

GET - päri olemasolevad parameetrite vaikeväärtused, näiteks:

GET http://localhost:8990/ipizza/defaults
 

DELETE - eemalda parameetrite vaikeväärtusi, näiteks:

DELETE http://localhost:8990/ipizza/defaults?VK_USER_NAME=
eemaldab VK_USER_NAME parameetri vaikeväärtuse

Kõik kolm eelnimetatud päringut tagastavad JSON kujul:
````
{
  "VK_OTHER": "Muu info",
  "VK_VERSION": "008",
  "VK_USER_NAME": "O’CONNEŽ-ŠUSLIK MARY ÄNN",
  "VK_TOKEN": "1",
  "VK_COUNTRY": "EE",
  "VK_USER_ID": "60001019906",
  "VK_SERVICE": "3013",
  "VK_RID": ""
}
````

## Pankade haldamise otspunkt: /ipizza/banks

PUT - lisa/määra pankasid, näiteks:

PUT http://localhost:8990/ipizza/banks?id=EYP&key={panga privaatvõti base64 kujul}
lisab panga, mis vastab päringule, milles VK_REC_ID=EYP, ning allkirjastab oma vastuse nimetatud privaatvõtmega; esialgne tagasipöördumismeetod on vaikimisi POST



GET - päri olemasolevate pankade andmeid, näiteks:

GET http://localhost:8990/ipizza/banks
Mõlemad päringud tagastavad JSON kujul:

````
[
  {
    "identifier": "EYP",
    "defaultParameters": {
      "VK_USER_NAME": "Kasutaja Nimi"
    }
    "callbackMethod": "POST"
  }
]
````


## Panga vastuse juhtimine

Individuaalse panga vaikeseadete otspunkt: /ipizza/banks/{panga identifikaator}/defaults

 

PUT - määra parameetrite vaikeväärtusi, näiteks:

PUT http://localhost:8990/ipizza/banks/EYP/defaults?VK_USER_NAME=Kasutaja%20Nimi
muudab/määrab vastava panga (EYP) vastava VK_USER_NAME parameetri vaikeväärtuseks "Kasutaja Nimi"

 

GET - päri olemasolevad parameetrite vaikeväärtused, näiteks:

GET http://localhost:8990/ipizza/banks/EYP/defaults
 

DELETE - eemalda parameetrite vaikeväärtusi, näiteks:

DELETE http://localhost:8990/ipizza/banks/EYP/defaults?VK_USER_NAME=
eemaldab VK_USER_NAME parameetri vaikeväärtuse

Kõik kolm eelnimetatud päringut tagastavad JSON kujul:
````
{
  "VK_USER_NAME": "Kasutaja Nimi"
}
````

## Tagasipöördumismeetodi juhtimine

Individuaalse panga tagasipöördumismeetodi määramise otspunkt: /ipizza/banks/{panga identifikaator}/method

Otspunkti poole saab pöörduda kõikvõimalike HTTP meetodiga, kasutatud meetod määrataksegi vastava panga tagasipöördumismeetodiks, näiteks:

GET http://localhost:8990/ipizza/banks/EYP/method
määrab tagasipöördumismeetodiks GET ning tagastab JSON kujul:

````
{
  "identifier": "EYP",
  "defaultParameters": {
    "VK_USER_NAME": "Kasutaja Nimi"
  },
  "callbackMethod": "GET"
}
````

## Vaikeseadistuse näidiskonfiguratsioon

Näidiskonfiguratsioon application.properties:

 
````
server.port = 8990

# List of mock return parameters with default values
ipizza.defaults.parameters = VK_SERVICE,VK_VERSION,VK_USER_NAME,VK_USER_ID,VK_COUNTRY,VK_OTHER,VK_TOKEN,VK_RID

# Default values for return parameters
ipizza.defaults.VK_SERVICE = 3013
ipizza.defaults.VK_VERSION = 008
ipizza.defaults.VK_USER_NAME = Kasutaja Nimi
ipizza.defaults.VK_USER_ID = 60001019906
ipizza.defaults.VK_COUNTRY = EE
ipizza.defaults.VK_OTHER = Muu info
ipizza.defaults.VK_TOKEN = 1
ipizza.defaults.VK_RID =
````