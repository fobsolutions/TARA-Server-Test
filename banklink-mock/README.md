# Pangalingiga autentimise mock teenus

## Kuidas toimib?

Mock teenus ootab `/ipizza/auth` otspunktil GET ja POST päringuid ning moodustab vastuseks vastavalt päringus leitud tagasipöördumisurlile (`VK_RETURN`) automaatse ümbersuunamisvormi koos autentimisvastusega.

Teenus lisab algses päringus tulnud `VK_SND_ID`, `VK_REC_ID`, `VK_NONCE`, `VK_RID`, `VK_ENCODING` ja `VK_LANG` väärtused vastusesse. Mocki vastuses kasutatavad muud väljad tagastatakse vastavalt seadistusele. Vaikimisi, kui panga jaoks pole tagastatavaid väärtuseid paika pandud (vt allpool toodud seadistamise näiteid), tagastatakse iga panga vastuses üldised vaikeväärtused vastavalt `application.properties` failis toodule (ja `/ipizza/defaults` otspunkti kaudu seadistatule).


## Paigaldamine ja käivitamine

1. Hangi TARA testid:

 `git clone https://github.com/e-gov/TARA-Server-Test.git`

2. Liigu pangalingiga autentimise mock teenuse kausta:

 `cd TARA-Server-Test/banklink-mock/`

3. Ehita rakendus:

 `mvn clean install`

4. Käivita rakendus:

 `java -jar target/banklink-mock-1.0.jar`

Rakendus hakkab kuulama konfiguratsioonifailis `application.properties` parameetri `server.port` poolt määratud porti.


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


**PUT** - määra parameetrite vaikeväärtusi, näiteks:
`PUT http://localhost:8990/ipizza/defaults?VK_USER_NAME=Kasutaja%20Nimi`
muudab/määrab `VK_USER_NAME` parameetri vaikeväärtuseks "Kasutaja Nimi"
 

**GET** - päri olemasolevad parameetrite vaikeväärtused, näiteks:
`GET http://localhost:8990/ipizza/defaults`
 

**DELETE** - eemalda parameetrite vaikeväärtusi, näiteks:
`DELETE http://localhost:8990/ipizza/defaults?VK_USER_NAME=`
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

**PUT** - lisa/määra pankasid, näiteks:
`PUT http://localhost:8990/ipizza/banks?id=EYP&key={panga privaatvõti base64 kujul}`
lisab panga, mis vastab päringule, milles `VK_REC_ID=EYP`, ning allkirjastab oma vastuse nimetatud privaatvõtmega; esialgne tagasipöördumismeetod on vaikimisi `POST`


**GET** - päri olemasolevate pankade andmeid, näiteks:
`GET http://localhost:8990/ipizza/banks`

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

Individuaalse panga vaikeseadete otspunkt: `/ipizza/banks/{panga identifikaator}/defaults`


**PUT** - määra parameetrite vaikeväärtusi, näiteks:
`PUT http://localhost:8990/ipizza/banks/EYP/defaults?VK_USER_NAME=Kasutaja%20Nimi`
muudab/määrab vastava panga (EYP) vastava `VK_USER_NAME` parameetri vaikeväärtuseks "Kasutaja Nimi"
 

**GET** - päri olemasolevad parameetrite vaikeväärtused, näiteks:
`GET http://localhost:8990/ipizza/banks/EYP/defaults`
 

**DELETE** - eemalda parameetrite vaikeväärtusi, näiteks:
`DELETE http://localhost:8990/ipizza/banks/EYP/defaults?VK_USER_NAME=`
eemaldab `VK_USER_NAME` parameetri vaikeväärtuse

Kõik kolm eelnimetatud päringut tagastavad JSON kujul:
````
{
  "VK_USER_NAME": "Kasutaja Nimi"
}
````

## Tagasipöördumismeetodi juhtimine

Individuaalse panga tagasipöördumismeetodi määramise otspunkt: `/ipizza/banks/{panga identifikaator}/method`

Otspunkti poole saab pöörduda kõikvõimalike HTTP meetodiga, kasutatud meetod määrataksegi vastava panga tagasipöördumismeetodiks, näiteks:
`GET http://localhost:8990/ipizza/banks/EYP/method`
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

Näidiskonfiguratsioon `application.properties`:

 
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
