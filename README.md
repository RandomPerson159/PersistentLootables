# Persistent Lootables
Functionality for time-based lootable containers

# Commands
/loot create mystery --> Uses the targeted container to create a mystery box <br />
/loot create individual --> Uses the targeted container to create an individual box <br />
/loot create everyone --> Uses the targeted container to create a shared everyone box <br />
/loot check --> Checks the targeted block for lootable data <br />
/loot set notify [true/false] --> Controls if the lootable should display information to the player <br />
/loot set time [minutes] --> Sets the time before loot will be generated again (can be set to zero for testing) <br />
/loot set amount [integer] --> Sets the amount of items to generate from the loot table <br />
/loot set items --> Opens the loot table for editing <br />

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
