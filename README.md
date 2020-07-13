# Multiplayer Death Swap
This is a spigot plugin for 1.15  that allows  you to play death swap.
It utilizes a scoring system to allow many people to play without the issues of an elimination system.
If you would like to play it with elimination, it is still possible. Simply remove a player from the game when they lose a point.

### Install
Download the jar file from the assets section of the [latest release](https://github.com/jeffreymeng/mcmultiplayerdeathswap/releases).

### Scoring

A swap is by default 5 minutes. The scoreboard on the right (shown once the game is started) displays points for each player, as well as the time before the next swap, in seconds.
Each 5 minute swap cycle, a the targets of each player will be sent to the chat. Each player is trying to build a trap for their target, thus the second player in a message will be teleported to the location of the first one.
If at any point after the initial 5 minutes a player dies, then they will lose a point to the player for whom they are the target. This can only happen once during a five minute period.

It is recommended that you tueb keep inventory on.

### Using the plugin

`/deathswap help` for command information.

`/deathswap players add [playername]`, to add a player.
`/deathswap players addAll` to add all players.
`/deathswap players remove [playername]`, to remove a player.
`/deathswap players removeAll` to remove a player.
`/deathswap players list` to view the current players.

`/deathswap timer set [seconds]` to set the timer length for each round. This can be done before or during a game.

`/deathswap start` to start the deathswap game.
`/deathswap stop` to stop the deathswap game.

`/deathswap swap` to set the current timer to 10 seeconds and trigger a swap manually.



### Development
This project uses maven.
You can build the jar file using  `$ mvn package`.
