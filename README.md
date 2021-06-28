## COINZ (CLANDESTINE OPERATION IMMINENT: NOCTURNAL ZIGGURAT)

Rado M. Kirilchev

Repository for the Informatics Large Practical 2018/19 course.


This is a multiplayer android game made from scratch. Unlike [Grabble](https://github.com/LumosX/grabble), which I developed in early 2017 (my penultimate undergrad year), this was made in late 2018 (during my Master's degree) and is significantly better in all possible aspects. Peek at the `ilp-report` pdf to see what it's all about. It's very pretty and has plenty of pictures.


### Details
Once again the professor was motivated by Pokemon Go, and thus wanted a game that integrates a map and geolocation. This time around we're walking around and collecting fictional "cryptocurrency coins". Per the specifications, the project utilises Google Firebase and Mapbox for its storage and mapping needs respectively.

Naturally, I created about two dozen extra features, adding bunches of extra mechanics into the game, as well as a narrative: a hidden war between two shadowy factions—an elite branch of the MI5 and a terrorist organisation—vying for control over a Soviet-era nuclear warhead stolen from the Kyrgyzstani government. I am naturally quite happy with the results of this project.

The name ("Coinz") was a part of the project specification; the "backronym" was my own invention.




### Game features:
(**Note:** Features in *italics* (i.e. only the first five bullet points) denote specification-requested features. All others are to be considered bonus features that I implemented of my own volition, that go beyond what was required for the course.)
* *Coins of the four fictional cryptocurrencies may be collected by walking within range.*
* *Collected coins are added to the respective "spare change" wallet balance.*
* *Coins from the "spare change" wallets may be deposited to the player's bank account.*
* *Deposits are limited to 25.0 total units of coins in a day.*
* *Coins from the "spare change" wallets may be sent to other players.*
* Upon registering for an account, the player selects a team. Teams provide different bonuses for players.
* The two teams differ in character and have different goals in the game's narrative *(more on this below)*.
* The player's bank account can hold balances in all five currencies: GOLD, DOLR, PENY, SHIL, QUID.
* Players may buy and sell any of the other four currencies for GOLD, at the daily rates, but after factoring in a commission percentage for the bank.
* All currencies can be used to purchase "Compute" (computing power) from team-specific "providers".
* "Compute" is then used to "decrypt" secret messages. Every message decrypted marginally improves the team's current position in the global struggle for victory.
* Whichever team decrypts most messages globally wins the game.
* Collecting coins, sending coins, and decrypting messages gives players "experience" points which are used for levelling-up.
* Increasing levels amplifies the team-specific bonuses received by the player.
* Wallet sizes, bank commission rates, and Compute prices are affected by the player's current level and team selection.
* Receiving coins from other players leaves a note in the player's "personal messages", serving a logging purpose. The sender's rank (title determined by his current level) is displayed as well.
* The game features a rudimentary narrative revolving around a missing nuclear bomb, named "Project Nocturnal Ziggurat", potentially held by terrorists.
* The first team, the "Eleventh Echelon", is a fictional division of the MI5 seeking to locate and retrieve the bomb before it falls into the wrong hands.
* The second team, the "Crimson Dawn", is a fictional group of high-tech mercenaries seeking to find the bomb and sell it to an unnamed buyer for a lot of money.
* The two teams feature distinct colour schemes: silver and black for the Eleventh Echelon, and crimson and black for the Crimson Dawn. The rest of the game's interface thematically fits, largely occupied by various shades of grey.

&nbsp;
### Repo structure
The **assets/*** folder contains the Photoshop source files for all drawables.  
The **coinz/*** folder is the complete Android Studio project folder.

