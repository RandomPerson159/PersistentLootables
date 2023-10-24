# Persistent Lootables
Functionality for time-based lootable containers

# Commands for containers
/ploot create mystery --> Uses the targeted container to create a mystery box <br />
/ploot create individual --> Uses the targeted container to create an individual box <br />
/ploot create everyone --> Uses the targeted container to create a shared everyone box <br />
/ploot check target --> Checks the targeted block for lootable data <br />
/ploot set notify [true/false] --> Controls if the lootable should display information to the player <br />
/ploot set time [minutes] --> Sets the time before loot will be generated again (can be set to zero for testing) <br />
/ploot set amount [integer] --> Sets the amount of items to generate from the loot table <br />
/ploot set items --> Opens the loot table for editing <br />
/ploot destroy target --> Removes all lootable data from the target container <br />
# Commands for chunks (heads/frames)
/ploot create head --> Makes the targeted head respawn in this chunk <br />
/ploot create frame --> Makes the trageted frame (get really close) respawn itself and its item in this chunk (if frame is destroyed it will respawn as an invisible one) <br />
/ploot check chunk --> Checks the current chunk for lootable data <br />
/ploot destroy chunk --> Destroys all lootable data for the chunk (heads/frames) <br />

# Box Types
The only difference between them is their default values and generation behaviour <br />

Mystery --> <br />
      amount = 1 <br />
      time = 30 minutes <br />
      notify = true <br />
      loot-per-player <br /><br />
Individual --> <br />
      amount = 27 <br />
      time = 60 minutes <br />
      notify = true <br />
      loot-per-player <br /><br />
Everyone --> <br />
      amount = 27 <br />
      time = 20 minutes <br />
      notify = false <br />
      loot-to-first-player <br /><br />

# Limitations
Use single block containers (no double chests) <br />
Works best with chests, barrels and shulker boxes <br />

# Tips
Lootable containers can be duplicated with middle click <br />
Once finished designing use /loot check <br />
