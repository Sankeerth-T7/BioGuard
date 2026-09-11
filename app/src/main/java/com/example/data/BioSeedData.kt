package com.example.data

import com.example.data.model.Achievement
import com.example.data.model.BioLocation
import com.example.data.model.ConservationTip
import com.example.data.model.Observation
import com.example.data.model.QuizQuestion
import com.example.data.model.Species
import com.example.data.model.ThreatReport

object BioSeedData {

    val speciesList: List<Species> = listOf(
        Species(
            id = "sp_peafowl",
            commonName = "Indian Peafowl",
            scientificName = "Pavo cristatus",
            category = "Birds",
            description = "The national bird of India, renowned for the iridescent blue-green plumage and magnificent courtship display of the male train.",
            habitat = "Dry deciduous and open scrub forests, agricultural edges, and sacred village groves.",
            ecologicalRole = "Seed disperser and omnivorous regulator of snakes, amphibians, and insect pest populations.",
            economicValue = "High cultural and ecotourism value; ancient symbol of biodiversity protection in agro-ecosystems.",
            threats = "Pesticide poisoning in agricultural zones, habitat fragmentation, and sporadic poaching for plumes.",
            conservationStatus = "Least Concern (IUCN Red List; Schedule I Wildlife Protection Act)",
            conservationAdvice = "Maintain tree corridors between villages and farmlands; prevent indiscriminate organophosphate pesticide usage.",
            geographicDistribution = "Indo-Gangetic plains, Deccan Plateau, Western & Eastern Ghats, Sri Lanka.",
            identifyingFeatures = listOf(
                "Striking metallic blue neck with fan-like crest on head",
                "Elongate upper-tail covert train bearing vibrant ocelli (eye-spots)",
                "Loud trumpet-like 'may-awe' territorial call heard at dawn"
            ),
            emoji = "🦚"
        ),
        Species(
            id = "sp_tiger",
            commonName = "Bengal Tiger",
            scientificName = "Panthera tigris tigris",
            category = "Mammals",
            description = "Apex carnivore and flagship umbrella species representing the ecological health of tropical Asian ecosystems.",
            habitat = "Tropical moist deciduous, mangrove swamps (Sundarbans), dry deciduous, and alluvial grasslands.",
            ecologicalRole = "Apex predator preventing herbivore overgrazing, thereby maintaining forest structure and hydrological watersheds.",
            economicValue = "Vital ecotourism anchor generating sustainable livelihood for hundreds of local buffer forest communities.",
            threats = "Poaching, human-wildlife conflict along corridor borders, linear infrastructure fragmentation, prey depletion.",
            conservationStatus = "Endangered (IUCN Red List; Appendix I CITES; Schedule I WPA)",
            conservationAdvice = "Restore degraded wildlife corridors (e.g., Corbett-Rajaji corridor) and support community eco-development committees.",
            geographicDistribution = "India (Ranthambore, Jim Corbett, Bandhavgarh, Sundarbans, Western Ghats), Bangladesh, Nepal, Bhutan.",
            identifyingFeatures = listOf(
                "Distinctive reddish-orange coat with black vertical stripes unique to each individual",
                "White belly and white spots behind large muscular ears (ocelli)",
                "Robust skull with powerful canine bite force exceeding 1,000 psi"
            ),
            emoji = "🐅"
        ),
        Species(
            id = "sp_elephant",
            commonName = "Asian Elephant",
            scientificName = "Elephas maximus",
            category = "Mammals",
            description = "A keystone ecological architect of Asian landscapes that creates forest clearings and waterholes used by hundreds of species.",
            habitat = "Tropical evergreen, semi-evergreen, moist deciduous, and scrub forests with access to perennial water.",
            ecologicalRole = "Seed dispersal mega-frugivore, canopy modifier, and mineral excavator facilitating access for smaller fauna.",
            economicValue = "Spiritual and environmental heritage mascot; drives landscape-scale catchment conservation.",
            threats = "Railway track collisions, electrocution from sagging transmission wires, habitat fragmentation, human-elephant crop conflict.",
            conservationStatus = "Endangered (IUCN Red List)",
            conservationAdvice = "Legally protect and clear elephant corridors from construction; install AI optical railway sensor systems.",
            geographicDistribution = "Western Ghats, North-East foothills, Central Indian elephant landscapes.",
            identifyingFeatures = listOf(
                "Smaller curved ears compared to African elephant",
                "Twin domed head profile with single finger-like process on trunk tip",
                "Male bulls usually bear tusks (tuskers) or lack tusks (makhnas)"
            ),
            emoji = "🐘"
        ),
        Species(
            id = "sp_squirrel",
            commonName = "Malabar Giant Squirrel",
            scientificName = "Ratufa indica",
            category = "Mammals",
            description = "One of the largest arboreal squirrels in the world, famous for its magnificent tricolor patchwork coat.",
            habitat = "High-canopy moist evergreen and moist deciduous forests of Peninsular India.",
            ecologicalRole = "Canopy seed distributor and indicator of undisturbed, continuous forest canopy connectivity.",
            economicValue = "Bioindicator species for Western Ghats rainforest health and canopy density.",
            threats = "Canopy disruption from highway expansions, logging, and monoculture plantation expansion (rubber/teak).",
            conservationStatus = "Least Concern (Sensitive to canopy loss)",
            conservationAdvice = "Construct canopy rope-bridges over road corridors cutting through tropical evergreen reserves.",
            geographicDistribution = "Western Ghats (Silent Valley, Shendurney, Bhimashankar) and pockets of Eastern Ghats.",
            identifyingFeatures = listOf(
                "Magnificent tri-colored fur (deep maroons, buffs, and creamy white)",
                "Elongated bushy tail exceeding body length (up to 60 cm)",
                "Builds multiple globular nests (dreys) in high tree branches"
            ),
            emoji = "🐿️"
        ),
        Species(
            id = "sp_banyan",
            commonName = "Indian Banyan Tree",
            scientificName = "Ficus benghalensis",
            category = "Plants",
            description = "The National Tree of India, an epiphytic strangler fig that develops vast sprawling canopies supported by aerial prop roots.",
            habitat = "Subtropical and tropical woodlands, roadsides, temple complexes, and riparian riverbanks.",
            ecologicalRole = "Keystone mutualist providing continuous synchronous fig crops sustaining bats, barbets, hornbills, and primates during lean dry months.",
            economicValue = "High Ayurvedic medicinal value (bark and latex used for anti-inflammatory treatments); shade and microclimate regulator.",
            threats = "Urban road-widening, urban concretization around root flares, invasive parasite vines.",
            conservationStatus = "Not Evaluated (Extensively protected culturally; keystone status)",
            conservationAdvice = "Propagate cuttings in social forestry schemes and protect sacred groves (Devarakadus) housing century-old specimens.",
            geographicDistribution = "Native to the Indian subcontinent; widely planted across tropical Asia.",
            identifyingFeatures = listOf(
                "Extensive aerial roots dangling and anchoring as secondary trunks",
                "Large leathery oval dark green leaves with conspicuous pale veins",
                "Paired scarlet-red rounded fig fruits nestled in leaf axils"
            ),
            emoji = "🌳"
        ),
        Species(
            id = "sp_neem",
            commonName = "Neem Tree",
            scientificName = "Azadirachta indica",
            category = "Plants",
            description = "Famed as 'Nature's Pharmacy', this drought-tolerant evergreen tree has been celebrated for millennia in indigenous agro-forestry.",
            habitat = "Arid, semi-arid, and dry tropical zones; thrives in nutrient-poor rocky soils.",
            ecologicalRole = "Natural insect repellent, soil detoxifier, and nitrogen balancer; provides thermal shelter in scorching summers.",
            economicValue = "Global bio-pesticide, organic neem cake fertilizer, cosmetics, and pharmacology sector worth billions.",
            threats = "Over-harvesting of wild timber, fungal dieback outbreaks in unmanaged monocultures.",
            conservationStatus = "Least Concern (Highly resilient species)",
            conservationAdvice = "Incorporate into social forestry, community green belts, and highway anti-pollution avenues.",
            geographicDistribution = "Indian Subcontinent, cultivated widely across dry tropics in Africa and Australia.",
            identifyingFeatures = listOf(
                "Pinnate compound leaves with serrated asymmetrical leaflets",
                "Fragrant white star-shaped blossoms during early spring",
                "Small smooth green drupes turning golden yellow upon ripening"
            ),
            emoji = "🌿"
        ),
        Species(
            id = "sp_cobra",
            commonName = "Spectacled Cobra",
            scientificName = "Naja naja",
            category = "Reptiles",
            description = "One of the 'Big Four' venomous snakes of India, possessing an iconic expandible hood with spectacled mark on the posterior.",
            habitat = "Agricultural fields, rocky terrain, rodent burrows, wetlands, and village peripheries.",
            ecologicalRole = "Crucial biological rodent controller, preventing massive grain harvest loss and rodent-borne leptospirosis.",
            economicValue = "Venom is essential for harvesting life-saving polyvalent antivenom serum; vital rodent predator saving agricultural crop yields.",
            threats = "Retaliatory killing due to fear, roadkill mortality, habitat loss, and illegal snake charming exploitation.",
            conservationStatus = "Schedule I Wildlife Protection Act (CITES Appendix II)",
            conservationAdvice = "Educate rural farmers on non-lethal snake rescue helplines and proper footwear in farmlands during night hours.",
            geographicDistribution = "Throughout mainland India, Pakistan, Sri Lanka, Bangladesh, and Nepal.",
            identifyingFeatures = listOf(
                "Expandable cervical hood displaying prominent 'spectacle' marking",
                "Smooth scales varying from yellow-olive to charcoal black",
                "Round pupil with prominent defensive upright hooding posture"
            ),
            emoji = "🐍"
        ),
        Species(
            id = "sp_turtle",
            commonName = "Olive Ridley Sea Turtle",
            scientificName = "Lepidochelys olivacea",
            category = "Reptiles",
            description = "Marine turtle world-famous for its synchronized mass nesting phenomena known as 'Arribada' on eastern coastal beaches.",
            habitat = "Open pelagic ocean, shallow coastal bays, and sandy tropical nesting beaches.",
            ecologicalRole = "Controls jellyfish blooms, balances ocean benthic seagrass beds, and transports vital marine nutrients onto coastal dune ecosystems.",
            economicValue = "Drives ethical community ecotourism and supports coastal beach conservation funds.",
            threats = "Trawler net drowning (bycatch), artificial beach lighting disorienting hatchlings, plastic pollution, coastal sea-wall constructions.",
            conservationStatus = "Vulnerable (IUCN Red List; Schedule I WPA)",
            conservationAdvice = "Mandate Turtle Excluder Devices (TEDs) on all mechanized trawl boats; shield coastal high-mast lights during nesting season.",
            geographicDistribution = "Gahirmatha & Rushikulya beaches (Odisha, India), Andaman & Nicobar, tropical Atlantic and Pacific.",
            identifyingFeatures = listOf(
                "Heart-shaped olive-gray carapace with 6 or more lateral scutes",
                "Paddle-like front flippers with one or two visible claws",
                "Synchronized nocturnal mass beaching of thousands of nesting females"
            ),
            emoji = "🐢"
        ),
        Species(
            id = "sp_purplefrog",
            commonName = "Purple Frog (Mahabali Frog)",
            scientificName = "Nasikabatrachus sahyadrensis",
            category = "Amphibians",
            description = "A living fossil discovered in 2003 with evolutionary lineage dating back 130 million years to the Jurassic era.",
            habitat = "Subterranean burrows in evergreen and moist deciduous forest leaf litter of the southern Western Ghats.",
            ecologicalRole = "Specialized fossorial termite predator, maintaining subterranean soil porosity and soil turnover.",
            economicValue = "Invaluable evolutionary phylogenetics resource; icon of Western Ghats biodiversity endemism.",
            threats = "Check-dam constructions drowning breeding torrents, road construction through breeding streams, agrochemical runoff.",
            conservationStatus = "Endangered (IUCN Red List)",
            conservationAdvice = "Designate micro-reserves around perennial hill streams where tadpoles anchor onto torrent rocks.",
            geographicDistribution = "Endemic exclusively to the Western Ghats of Kerala and Tamil Nadu, India.",
            identifyingFeatures = listOf(
                "Dark purple-grey bloated body with tiny beady eyes and pointed white snout",
                "Spade-like muscular hind legs adapted for rapid underground burrowing",
                "Emerged above-ground only for 1-2 days annually during monsoon for breeding"
            ),
            emoji = "🐸"
        ),
        Species(
            id = "sp_hornbill",
            commonName = "Great Pied Hornbill",
            scientificName = "Buceros bicornis",
            category = "Birds",
            description = "A magnificent forest hornbill with a massive curved yellow bill surmounted by a hollow keratinous casque.",
            habitat = "Primary wet evergreen and semi-evergreen hill forests with old-growth emergent trees.",
            ecologicalRole = "'Farmer of the Forest' – primary long-distance disperser of large-seeded tropical rainforest trees.",
            economicValue = "Cultural totem of indigenous tribes (e.g. Nyishi, Hornbill Festival in Nagaland) promoting community conservation.",
            threats = "Felling of hollow nesting trees, habitat fragmentation, hunting for decorative tail feathers and beaks.",
            conservationStatus = "Vulnerable (IUCN Red List; Schedule I WPA)",
            conservationAdvice = "Support the Hornbill Nest Adoption Program where indigenous community protectors guard active nest hollows.",
            geographicDistribution = "Western Ghats, North-East Himalayan foothills, South-East Asia.",
            identifyingFeatures = listOf(
                "Massive yellow and black bill with a prominent U-shaped casque atop",
                "Contrasting black and white plumage with white neck and banded tail",
                "Loud swooshing wingbeats audible from several hundred meters away"
            ),
            emoji = "🦜"
        ),
        Species(
            id = "sp_butterfly",
            commonName = "Blue Mormon Butterfly",
            scientificName = "Papilio polymnestor",
            category = "Insects",
            description = "One of the largest swallowtail butterflies in India and the State Butterfly of Maharashtra, celebrated for its velvety black and electric-cyan wings.",
            habitat = "Heavy rainfall evergreen forests, deciduous valleys, gardens, and citrus orchards.",
            ecologicalRole = "Essential pollinator for native forest flora, wild citrus, and diverse riparian flowering trees.",
            economicValue = "Bio-indicator of tropical forest humidity, canopy moisture, and pesticide-free riparian microclimates.",
            threats = "Deforestation, heavy pesticide applications in adjoining agricultural borders.",
            conservationStatus = "Locally common, protected within Western Ghats sanctuaries.",
            conservationAdvice = "Plant native larval host plants (such as Atalantia racemosa and wild lime) in buffer community zones.",
            geographicDistribution = "Endemic to Peninsular India and Sri Lanka; dominant in Western Ghats.",
            identifyingFeatures = listOf(
                "Velvety black upper forewings with pale greenish-blue outer band",
                "Bright electric azure-blue scaling across upper hindwings with black spots",
                "Wingspan reaching up to 150 mm with swift undulating flight"
            ),
            emoji = "🦋"
        ),
        Species(
            id = "sp_ecosystem_mangrove",
            commonName = "Sundarbans Mangrove Ecosystem",
            scientificName = "Rhizophora & Avicennia Coastal Biosphere",
            category = "Ecosystems",
            description = "The largest contiguous tidal halophytic mangrove forest in the world, spanning the delta formed by the Ganga, Brahmaputra, and Meghna rivers.",
            habitat = "Intertidal coastal delta zones, brackish mudflats, and estuarine river channels.",
            ecologicalRole = "Global carbon sink (blue carbon), coastal bio-shield against tropical cyclones and tidal surges, nursery for marine fisheries.",
            economicValue = "Provides protection worth billions of dollars against catastrophic storms; sustains wild honey collectors and artisanal fisheries.",
            threats = "Sea-level rise from climate change, increased cyclone frequency, reduced freshwater river flow, upstream industrial effluents.",
            conservationStatus = "UNESCO World Heritage Site & Ramsar Wetland of International Importance",
            conservationAdvice = "Replant climate-resilient mangrove saplings, establish community disaster-warning eco-guards, and regulate upstream freshwater diversions.",
            geographicDistribution = "West Bengal (India) and southern Bangladesh delta.",
            identifyingFeatures = listOf(
                "Pneumatophores (pencil roots) sticking out vertically through saline mud for breathing",
                "Stilt roots and viviparous seedlings germinating while still attached to mother trees",
                "Home to swimming Bengal tigers, estuarine crocodiles, and river dolphins"
            ),
            emoji = "🏞️"
        )
    )

    val locations: List<BioLocation> = listOf(
        BioLocation(
            id = "loc_hemis",
            name = "Hemis National Park",
            state = "Ladakh",
            type = "National Park",
            description = "High-altitude national park in eastern Ladakh, globally famous as the snow leopard capital of the world.",
            importance = "Crucial trans-Himalayan cold desert sanctuary preserving fragile alpine high-plateau fauna and Tibetan wolves.",
            keySpecies = listOf("Snow Leopard", "Tibetan Wolf", "Bharal (Blue Sheep)", "Golden Eagle", "Himalayan Marmot"),
            xPercent = 0.319f,
            yPercent = 0.107f,
            coordinatesStr = "33.9167° N, 77.4000° E"
        ),
        BioLocation(
            id = "loc_great_himalayan",
            name = "Great Himalayan National Park",
            state = "Himachal Pradesh",
            type = "National Park",
            description = "UNESCO World Heritage Site in the western Himalayas protecting pristine alpine meadows, glacial valleys, and oak-conifer forests.",
            importance = "Critical stronghold for the Western Tragopan, Himalayan Musk Deer, and rare medicinal alpine flora.",
            keySpecies = listOf("Western Tragopan", "Himalayan Musk Deer", "Himalayan Brown Bear", "Snow Leopard", "Blue Sheep"),
            xPercent = 0.317f,
            yPercent = 0.178f,
            coordinatesStr = "31.7500° N, 77.3400° E"
        ),
        BioLocation(
            id = "loc_corbett",
            name = "Jim Corbett National Park",
            state = "Uttarakhand",
            type = "National Park",
            description = "India's oldest national park established in 1936, nestled in the Himalayan Shivalik foothills along the Ramganga river.",
            importance = "Crucial habitat for Bengal Tiger, Asian Elephant, and over 600 migratory and resident avifaunal species.",
            keySpecies = listOf("Bengal Tiger", "Asian Elephant", "Gharial", "Hog Deer", "Tawny Fish Owl"),
            xPercent = 0.365f,
            yPercent = 0.250f,
            coordinatesStr = "29.5300° N, 78.7747° E"
        ),
        BioLocation(
            id = "loc_keoladeo",
            name = "Keoladeo Ghana National Park",
            state = "Rajasthan",
            type = "Wildlife Sanctuary",
            description = "UNESCO World Heritage wetland sanctuary hosting over 370 bird species and critical wintering grounds for migratory waterfowl.",
            importance = "Ramsar wetland providing essential staging and breeding wetlands along the Central Asian Flyway.",
            keySpecies = listOf("Sarus Crane", "Painted Stork", "Siberian Rubythroat", "Fishing Cat", "Rock Python"),
            xPercent = 0.323f,
            yPercent = 0.327f,
            coordinatesStr = "27.1594° N, 77.5233° E"
        ),
        BioLocation(
            id = "loc_ranthambore",
            name = "Ranthambore Tiger Reserve",
            state = "Rajasthan",
            type = "National Park",
            description = "Historic wilderness where scrub forests, banyan lakes, and an ancient 10th-century fortress shelter wild predator populations.",
            importance = "Critical genetic reserve connecting Aravalli and Vindhya hill systems for semi-arid fauna.",
            keySpecies = listOf("Bengal Tiger", "Sloth Bear", "Marsh Crocodile", "Caracal", "Indian Leopard"),
            xPercent = 0.288f,
            yPercent = 0.364f,
            coordinatesStr = "26.0173° N, 76.5026° E"
        ),
        BioLocation(
            id = "loc_desert_np",
            name = "Desert National Park",
            state = "Rajasthan",
            type = "National Park",
            description = "Vast desert ecosystem near Jaisalmer featuring shifting dunes, craggy rocks, and fossilized ancient wood.",
            importance = "Sole vital refuge for the Critically Endangered Great Indian Bustard and Thar desert fox.",
            keySpecies = listOf("Great Indian Bustard", "Desert Fox", "Chinkara (Indian Gazelle)", "Spiny-tailed Lizard", "Tawny Eagle"),
            xPercent = 0.098f,
            yPercent = 0.335f,
            coordinatesStr = "26.9100° N, 70.9000° E"
        ),
        BioLocation(
            id = "loc_gir",
            name = "Gir National Park & Sanctuary",
            state = "Gujarat",
            type = "Wildlife Sanctuary",
            description = "The exclusive last wild sanctuary on Earth for the majestic Asiatic Lion (Panthera leo persica).",
            importance = "Dry teak and scrub forest ecosystem demonstrating remarkable community coexistence with Maldhari pastoralists.",
            keySpecies = listOf("Asiatic Lion", "Indian Leopard", "Chousingha (Four-horned Antelope)", "Striped Hyena", "Mugger Crocodile"),
            xPercent = 0.096f,
            yPercent = 0.524f,
            coordinatesStr = "21.1241° N, 70.8242° E"
        ),
        BioLocation(
            id = "loc_kanha",
            name = "Kanha Tiger Reserve",
            state = "Madhya Pradesh",
            type = "National Park",
            description = "Vast lush Sal and bamboo forests and expansive open meadows in the Maikal hills that inspired Kipling's Jungle Book.",
            importance = "Sole surviving wild home of the southern Swamp Deer (Hard-ground Barasingha) brought back from near-extinction.",
            keySpecies = listOf("Bengal Tiger", "Hard-ground Barasingha", "Indian Leopard", "Dhole", "Indian Gaur"),
            xPercent = 0.428f,
            yPercent = 0.484f,
            coordinatesStr = "22.3345° N, 80.6115° E"
        ),
        BioLocation(
            id = "loc_tadoba",
            name = "Tadoba Andhari Tiger Reserve",
            state = "Maharashtra",
            type = "National Park",
            description = "Oldest and largest national park in Maharashtra, featuring dense bamboo and teak forests around Tadoba lake.",
            importance = "High-density breeding corridor for central Indian Bengal tigers and sloth bears.",
            keySpecies = listOf("Bengal Tiger", "Sloth Bear", "Wild Dog (Dhole)", "Indian Leopard", "Marsh Crocodile"),
            xPercent = 0.383f,
            yPercent = 0.552f,
            coordinatesStr = "20.2400° N, 79.3000° E"
        ),
        BioLocation(
            id = "loc_sundarbans",
            name = "Sundarbans Biosphere Reserve",
            state = "West Bengal",
            type = "Biosphere Reserve",
            description = "The world's largest mangrove delta, formed by the confluence of the Ganges and Brahmaputra rivers in the Bay of Bengal.",
            importance = "Global blue carbon storehouse, cyclone mitigation buffer, and sole habitat of swimming coastal Bengal Tigers.",
            keySpecies = listOf("Bengal Tiger", "Saltwater Crocodile", "Irrawaddy Dolphin", "Mangrove Horseshoe Crab", "Olive Ridley"),
            xPercent = 0.718f,
            yPercent = 0.497f,
            coordinatesStr = "21.9497° N, 89.1833° E"
        ),
        BioLocation(
            id = "loc_kaziranga",
            name = "Kaziranga National Park",
            state = "Assam",
            type = "National Park",
            description = "A UNESCO World Heritage Site hosting two-thirds of the world's Great One-horned Rhinoceroses along the Brahmaputra floodplain.",
            importance = "High-density grassland ecosystem providing refuge to mega-herbivores and breeding waterbirds.",
            keySpecies = listOf("One-horned Rhinoceros", "Wild Water Buffalo", "Eastern Swamp Deer", "Tiger", "Bengal Florican"),
            xPercent = 0.853f,
            yPercent = 0.346f,
            coordinatesStr = "26.5775° N, 93.1711° E"
        ),
        BioLocation(
            id = "loc_manas",
            name = "Manas National Park & Biosphere",
            state = "Assam",
            type = "Biosphere Reserve",
            description = "Contiguous biodiversity reserve situated at the foothills of the Bhutan Himalayas along the Manas river.",
            importance = "Pioneering community-based tiger and pygmy hog reintroduction reserve and elephant corridor.",
            keySpecies = listOf("Pygmy Hog", "Golden Langur", "Hispid Hare", "Wild Water Buffalo", "One-horned Rhinoceros"),
            xPercent = 0.781f,
            yPercent = 0.342f,
            coordinatesStr = "26.7100° N, 91.0300° E"
        ),
        BioLocation(
            id = "loc_namdapha",
            name = "Namdapha National Park",
            state = "Arunachal Pradesh",
            type = "National Park",
            description = "Vast evergreen montane rainforest rising into the Eastern Himalayas, hosting unmatched altitudinal biodiversity.",
            importance = "Only park in the world harboring four feline species: Tiger, Leopard, Snow Leopard, and Clouded Leopard.",
            keySpecies = listOf("Clouded Leopard", "Hoolock Gibbon", "Namdapha Flying Squirrel", "Great Hornbill", "Red Panda"),
            xPercent = 0.962f,
            yPercent = 0.316f,
            coordinatesStr = "27.5000° N, 96.3800° E"
        ),
        BioLocation(
            id = "loc_khangchendzonga",
            name = "Khangchendzonga Biosphere Reserve",
            state = "Sikkim",
            type = "Biosphere Reserve",
            description = "UNESCO mixed World Heritage site spanning sacred Himalayan peaks, glaciers, and alpine rhododendron valleys.",
            importance = "Unique trans-Himalayan high-elevation wildlife haven and sacred Tibetan Buddhist cultural landscape.",
            keySpecies = listOf("Snow Leopard", "Red Panda", "Tibetan Sheep (Argali)", "Himalayan Tahr", "Blood Pheasant"),
            xPercent = 0.683f,
            yPercent = 0.310f,
            coordinatesStr = "27.7000° N, 88.1600° E"
        ),
        BioLocation(
            id = "loc_western_ghats",
            name = "Western Ghats Biodiversity Hotspot",
            state = "Kerala, Karnataka, Tamil Nadu, Maharashtra",
            type = "Biodiversity Hotspot",
            description = "An ancient mountain chain older than the Himalayas, featuring exceptional levels of biological endemism.",
            importance = "One of the 36 Global Biodiversity Hotspots, sheltering hundreds of globally threatened species and perennial river sources.",
            keySpecies = listOf("Lion-tailed Macaque", "Nilgiri Tahr", "Purple Frog", "Malabar Giant Squirrel", "Great Hornbill"),
            xPercent = 0.289f,
            yPercent = 0.849f,
            coordinatesStr = "11.1271° N, 76.5197° E"
        ),
        BioLocation(
            id = "loc_bandipur",
            name = "Bandipur National Park",
            state = "Karnataka",
            type = "National Park",
            description = "Part of the Nilgiri Biosphere Reserve connecting Nagarhole, Wayanad, and Mudumalai across deciduous forests.",
            importance = "High tiger density sanctuary and major seasonal migratory crossing for Asian elephant herds.",
            keySpecies = listOf("Asian Elephant", "Bengal Tiger", "Indian Gaur", "Four-horned Antelope", "Mugger Crocodile"),
            xPercent = 0.293f,
            yPercent = 0.832f,
            coordinatesStr = "11.6700° N, 76.6300° E"
        ),
        BioLocation(
            id = "loc_nilgiri",
            name = "Nilgiri Biosphere Reserve",
            state = "Tamil Nadu, Kerala, Karnataka",
            type = "Biosphere Reserve",
            description = "India's very first Biosphere Reserve designated by UNESCO in 1986, uniting Mudumalai, Bandipur, and Silent Valley.",
            importance = "Hosts largest Asian elephant breeding population in Asia and unique Shola-Grassland montane ecosystems.",
            keySpecies = listOf("Nilgiri Tahr", "Asian Elephant", "Tiger", "Dhole (Asiatic Wild Dog)", "Neelakurinji Flower"),
            xPercent = 0.294f,
            yPercent = 0.840f,
            coordinatesStr = "11.4167° N, 76.6833° E"
        ),
        BioLocation(
            id = "loc_periyar",
            name = "Periyar National Park & Tiger Reserve",
            state = "Kerala",
            type = "National Park",
            description = "Protected evergreen and semi-evergreen catchment around an artificial lake in the Cardamom Hills.",
            importance = "Pioneering model for participatory community eco-tourism, anti-poaching eco-development, and elephant herd preservation.",
            keySpecies = listOf("Asian Elephant", "Bengal Tiger", "Nilgiri Langur", "Salim Ali's Fruit Bat", "Malabar Hornbill"),
            xPercent = 0.310f,
            yPercent = 0.903f,
            coordinatesStr = "9.4667° N, 77.1400° E"
        ),
        BioLocation(
            id = "loc_great_nicobar",
            name = "Great Nicobar Biosphere Reserve",
            state = "Andaman and Nicobar Islands",
            type = "Biosphere Reserve",
            description = "Pristine tropical rainforest island in the eastern Indian Ocean harboring ancient marine ecosystems and isolated endemic tribes.",
            importance = "Rich coastal coral reefs, nesting grounds for Giant Leatherback sea turtles and Nicobar Megapodes.",
            keySpecies = listOf("Giant Leatherback Turtle", "Nicobar Megapode", "Saltwater Crocodile", "Nicobar Tree Shrew", "Dugong"),
            xPercent = 0.877f,
            yPercent = 0.986f,
            coordinatesStr = "6.9272° N, 93.8580° E",
            latitude = 6.9272,
            longitude = 93.8580,
            areaSqKm = 885.0,
            elevationProfile = "0 - 642 m (Mount Thullier)",
            establishedYear = 2013
        ),
        BioLocation(
            id = "loc_dachigam",
            name = "Dachigam National Park",
            state = "Jammu & Kashmir",
            type = "National Park",
            description = "Nestled in the Zabarwan range of the western Himalayas, guarding the pristine watershed of Dal Lake.",
            importance = "Sole surviving sanctuary for the critically endangered Hangul (Kashmir Stag) and Himalayan black bear.",
            keySpecies = listOf("Hangul (Kashmir Stag)", "Himalayan Black Bear", "Himalayan Brown Bear", "Leopard Cat", "Monal Pheasant"),
            xPercent = 0.285f,
            yPercent = 0.125f,
            coordinatesStr = "34.1372° N, 75.0353° E",
            latitude = 34.1372,
            longitude = 75.0353,
            areaSqKm = 141.0,
            elevationProfile = "1,600 - 4,200 m (Alpine ridges)",
            establishedYear = 1981
        ),
        BioLocation(
            id = "loc_nanda_devi",
            name = "Nanda Devi & Valley of Flowers",
            state = "Uttarakhand",
            type = "Biosphere Reserve",
            description = "High-altitude Himalayan glacial wilderness dominated by India's second-highest peak and breathtaking endemic alpine blossom meadows.",
            importance = "UNESCO World Heritage site protecting rare medicinal alpine herbs, snow leopards, and pristine glacial valleys.",
            keySpecies = listOf("Snow Leopard", "Himalayan Musk Deer", "Bharal", "Asiatic Black Bear", "Brahmakamal Flower"),
            xPercent = 0.380f,
            yPercent = 0.235f,
            coordinatesStr = "30.4167° N, 79.9167° E",
            latitude = 30.4167,
            longitude = 79.9167,
            areaSqKm = 630.3,
            elevationProfile = "3,200 - 7,816 m (Nanda Devi summit)",
            establishedYear = 1988
        ),
        BioLocation(
            id = "loc_bandhavgarh",
            name = "Bandhavgarh Tiger Reserve",
            state = "Madhya Pradesh",
            type = "National Park",
            description = "Cradle of the white tiger in the Vindhya range, famed for one of the highest densities of Royal Bengal Tigers in the world.",
            importance = "Core protected corridor of dry deciduous and moist Sal forests interspersed with ancient caves and sheer sandstone cliffs.",
            keySpecies = listOf("Royal Bengal Tiger", "Indian Leopard", "Spotted Deer (Chital)", "Sambar", "Chausingha"),
            xPercent = 0.435f,
            yPercent = 0.450f,
            coordinatesStr = "23.7019° N, 81.0264° E",
            latitude = 23.7019,
            longitude = 81.0264,
            areaSqKm = 1536.0,
            elevationProfile = "410 - 810 m (Bandhavgarh Fort ridge)",
            establishedYear = 1968
        ),
        BioLocation(
            id = "loc_similipal",
            name = "Similipal Biosphere Reserve",
            state = "Odisha",
            type = "Biosphere Reserve",
            description = "Vast forested plateau in northern Odisha home to majestic waterfalls (Joranda and Barehipani), melanistic black tigers, and sal forests.",
            importance = "UNESCO biosphere harboring the rare wild gene pool of pseudo-melanistic Royal Bengal tigers and 96 orchid species.",
            keySpecies = listOf("Melanistic Tiger", "Asian Elephant", "Chausingha", "Mugger Crocodile", "Barehipani Orchids"),
            xPercent = 0.585f,
            yPercent = 0.520f,
            coordinatesStr = "21.8617° N, 86.3475° E",
            latitude = 21.8617,
            longitude = 86.3475,
            areaSqKm = 2750.0,
            elevationProfile = "300 - 1,165 m (Khairiburu peak)",
            establishedYear = 1994
        ),
        BioLocation(
            id = "loc_gulf_mannar",
            name = "Gulf of Mannar Marine Biosphere",
            state = "Tamil Nadu",
            type = "Biosphere Reserve",
            description = "India's first marine biosphere reserve, encompassing 21 uninhabited coral islands, seagrass beds, and mangrove estuaries.",
            importance = "Crucial Indian Ocean refuge for the endangered Dugong (sea cow), sea turtles, and over 117 coral reef species.",
            keySpecies = listOf("Dugong (Sea Cow)", "Green Sea Turtle", "Whale Shark", "Sea Cucumber", "Brain Coral"),
            xPercent = 0.355f,
            yPercent = 0.915f,
            coordinatesStr = "9.1300° N, 79.1200° E",
            latitude = 9.1300,
            longitude = 79.1200,
            areaSqKm = 10500.0,
            elevationProfile = "0 - 15 m (Coastal shelf & reefs)",
            establishedYear = 1989
        ),
        BioLocation(
            id = "loc_nokrek",
            name = "Nokrek Biosphere Reserve",
            state = "Meghalaya",
            type = "Biosphere Reserve",
            description = "Lush montane cloud forest in the Garo Hills protecting the natural gene sanctuary for wild citrus (Citrus indica).",
            importance = "Global biodiversity reservoir for wild progenitors of cultivated citrus fruits, red panda, and Asian elephants.",
            keySpecies = listOf("Hoolock Gibbon", "Red Panda", "Wild Citrus (Citrus indica)", "Slow Loris", "Stump-tailed Macaque"),
            xPercent = 0.760f,
            yPercent = 0.380f,
            coordinatesStr = "25.4833° N, 90.3167° E",
            latitude = 25.4833,
            longitude = 90.3167,
            areaSqKm = 820.0,
            elevationProfile = "465 - 1,412 m (Nokrek Peak)",
            establishedYear = 1988
        )
    )

    val conservationTips: List<ConservationTip> = listOf(
        ConservationTip(
            id = "tip_native_plants",
            category = "Plants",
            title = "Plant Indigenous Native Flora",
            description = "Exotic ornamental plants often fail to nourish local insect caterpillars and pollinators. Native shrubs support 10x more pollinator species.",
            actionStep = "Choose native trees like Neem, Banyan, Jamun, or Kadamba instead of invasive eucalyptus or bougainvillea in gardens."
        ),
        ConservationTip(
            id = "tip_water_conservation",
            category = "Water",
            title = "Protect Local Freshwater Catchments",
            description = "Aquatic ecosystems are rapidly declining due to pesticide wash-off, microplastics, and groundwater table depletion.",
            actionStep = "Avoid flushing toxic synthetic detergents or motor chemicals into storm drains; install rooftop rainwater harvesting barrels."
        ),
        ConservationTip(
            id = "tip_wildlife_distance",
            category = "Wildlife",
            title = "Maintain Ethical Wildlife Distance",
            description = "Approaching nesting birds or wild mammals causes chronic stress hormones, nest abandonment, and predatory vulnerability.",
            actionStep = "Never bait or feed wild animals; use telephoto optics or binoculars to observe them in silence without disrupting their feeding cycles."
        ),
        ConservationTip(
            id = "tip_campus_audit",
            category = "Campus",
            title = "Campus Biodiversity Mapping & Social Forestry",
            description = "College and university campuses often possess untended corners that can become micro-habitats and pollinator sanctuaries.",
            actionStep = "Create a student biodiversity register, plant a butterfly nectar garden, and designate 'no-mow' wild wildflower patches."
        ),
        ConservationTip(
            id = "tip_waste_reduction",
            category = "Waste",
            title = "Eliminate Single-Use Plastics in Riparian Zones",
            description = "Riverine plastics break down into toxic microplastics consumed by freshwater fish, turtles, and terrestrial birds.",
            actionStep = "Switch to reusable steel bottles, organize bi-weekly river cleanup drives, and compost organic organic campus kitchen scraps."
        ),
        ConservationTip(
            id = "tip_light_pollution",
            category = "Home",
            title = "Curtail Disruptive Nocturnal Light Pollution",
            description = "Harsh high-mast white light disorients nocturnal moths, migratory birds, hatching marine turtles, and fireflies.",
            actionStep = "Use warm shielded amber LED bulbs (<2700K) directed downwards and turn off unneeded outdoor spotlights after 10 PM."
        ),
        ConservationTip(
            id = "tip_sustainable_timber",
            category = "Forests",
            title = "Support Certified Sustainable Wood & Paper",
            description = "Illegal clear-cutting of old-growth rainforests destroys ancient micro-climates that take hundreds of years to regenerate.",
            actionStep = "Look for FSC (Forest Stewardship Council) certified post-consumer recycled paper and avoid teak harvested from unverified logging concessions."
        ),
        ConservationTip(
            id = "tip_citizen_science",
            category = "Community",
            title = "Participate in BioGuard Citizen Science",
            description = "Ecologists rely on geo-tagged community observations to track migratory shift patterns, invasive weed spreads, and breeding recoveries.",
            actionStep = "Log observations of birds, insects, and flora regularly on BioGuard to build public awareness and long-term conservation datasets."
        )
    )

    val quizQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = 1,
            category = "Biodiversity",
            question = "Which of the following defines a global 'Biodiversity Hotspot'?",
            options = listOf(
                "A region with high tourist footfall and desert wildlife",
                "A region containing at least 1,500 endemic vascular plants that has lost ≥70% of its original primary habitat",
                "Any area declared as a private game reserve with artificial irrigation",
                "An urban zoo containing endangered species from around the world"
            ),
            correctIndex = 1,
            explanation = "According to Norman Myers' criteria, a biodiversity hotspot must contain at least 1,500 species of endemic vascular plants and must have lost at least 70% of its original primary habitat."
        ),
        QuizQuestion(
            id = 2,
            category = "Fauna",
            question = "Which ecological term best describes the Asian Elephant due to its profound physical modification of forest habitats?",
            options = listOf(
                "Parasitic consumer",
                "Ecosystem Engineer / Keystone Species",
                "Secondary decomposer",
                "Invasive alien species"
            ),
            correctIndex = 1,
            explanation = "Asian elephants are ecological engineers: by toppling trees, digging waterholes, and dispersing seeds over long distances, they shape the physical habitat for countless smaller species."
        ),
        QuizQuestion(
            id = 3,
            category = "Conservation",
            question = "What is the primary operational difference between a 'National Park' and a 'Wildlife Sanctuary' in India?",
            options = listOf(
                "National parks have no wild animals",
                "Sanctuaries do not allow any research work",
                "No human activity (like grazing or private land tenure) is permitted in a National Park without Chief Wildlife Warden approval, whereas limited traditional rights may be permitted in a Sanctuary",
                "Wildlife Sanctuaries are exclusively privately owned"
            ),
            correctIndex = 2,
            explanation = "Under the Wildlife Protection Act, 1972, a National Park enjoys higher statutory protection where grazing of cattle and commercial activities are strictly prohibited, whereas limited regulated rights can be permitted in Sanctuaries."
        ),
        QuizQuestion(
            id = 4,
            category = "Flora",
            question = "Why are Ficus (Banyan, Peepal, Fig) species classified as 'Keystone Mutualists' in tropical ecosystems?",
            options = listOf(
                "Their wood is completely fire-proof",
                "They produce asynchronous fruit crops sustaining frugivores when other forest trees are dormant",
                "They prevent all other undergrowth plants from growing beneath them",
                "They repel all insects through chemical pheromones"
            ),
            correctIndex = 1,
            explanation = "Figs bear fruit year-round in asynchronous cycles. During lean dry winter periods when few other trees fruit, figs sustain birds, bats, and primates, preventing starvation."
        ),
        QuizQuestion(
            id = 5,
            category = "Threats",
            question = "What is considered the single leading global driver of terrestrial biodiversity loss today?",
            options = listOf(
                "Volcanic ash eruptions",
                "Habitat destruction and land fragmentation from agricultural expansion and linear infrastructure",
                "Asteroid impacts",
                "Natural seasonal rainfall variations"
            ),
            correctIndex = 1,
            explanation = "The IPBES global assessment confirms that habitat degradation and fragmentation driven by land conversion for agriculture and infrastructure is the foremost cause of species extinction."
        ),
        QuizQuestion(
            id = 6,
            category = "Ecological Restoration",
            question = "What is the core principle of 'Ecological Restoration' compared to simple commercial tree plantation?",
            options = listOf(
                "Planting only fast-growing timber like Eucalyptus for quick commercial timber sale",
                "Re-establishing the native species composition, structural diversity, and self-sustaining ecosystem processes of the indigenous biome",
                "Clearing native grasslands to plant commercial fruit orchards",
                "Covering wetlands with concrete embankments"
            ),
            correctIndex = 1,
            explanation = "Ecological restoration focuses on recovering degraded ecosystems using native multi-tier plant species and soil biology, rather than monoculture timber plantations that offer little habitat value."
        ),
        QuizQuestion(
            id = 7,
            category = "Social Forestry",
            question = "What is the main objective of 'Social Forestry' in environmental management?",
            options = listOf(
                "Encouraging forestry management by and for rural communities on community wastelands, farm borders, and village commons",
                "Exporting wild animals to foreign safari parks",
                "Cutting down reserved forests for industrial timber mills",
                "Converting all natural wetlands into swimming pools"
            ),
            correctIndex = 0,
            explanation = "Social forestry involves the management and protection of forests and afforestation on barren lands with the purpose of helping in the environmental, social, and rural development of communities."
        ),
        QuizQuestion(
            id = 8,
            category = "Indian Biodiversity",
            question = "Which of the following is an endemic amphibian living almost entirely underground in the Western Ghats of India?",
            options = listOf(
                "Bullfrog",
                "Purple Frog (Nasikabatrachus sahyadrensis)",
                "Common Indian Toad",
                "Tree Frog"
            ),
            correctIndex = 1,
            explanation = "The Purple Frog (Mahabali Frog) is a living evolutionary fossil endemic to the Western Ghats that spends 99% of its life burrowed underground, surfacing only for a couple of days during monsoons."
        ),
        QuizQuestion(
            id = 9,
            category = "Ecosystems",
            question = "What critical ecological role do coastal mangrove forests play in shoreline protection?",
            options = listOf(
                "They increase water salinity to toxic levels",
                "Their complex stilt root matrices dissipate up to 66% of wave energy and protect shorelines from hurricane storm surges",
                "They dry up coastal soils to facilitate real estate developments",
                "They emit smoke that drives away storms"
            ),
            correctIndex = 1,
            explanation = "Mangroves act as living bio-shields. Their dense prop root networks trap sediments, stabilize coastline mudflats, and dramatically dampen the energy of tidal waves and storm surges."
        ),
        QuizQuestion(
            id = 10,
            category = "Conservation",
            question = "What is an 'Umbrella Species' in conservation biology?",
            options = listOf(
                "A species that uses leaves to shield itself from monsoon rains",
                "A wide-ranging species whose habitat protection automatically preserves many other co-occurring species in that ecosystem",
                "A domestic species kept in home gardens",
                "An organism that exclusively lives in deep underground caverns"
            ),
            correctIndex = 1,
            explanation = "Umbrella species (like the Bengal Tiger or Asian Elephant) have large ecological footprint requirements. Protecting the vast forested habitat they need protects thousands of insects, birds, and smaller plants beneath their 'umbrella'."
        )
    )

    val achievements: List<Achievement> = listOf(
        Achievement(
            id = "ach_first_discovery",
            title = "First Discovery",
            description = "Identify your very first species with AI.",
            badgeEmoji = "🌱",
            pointsReward = 50,
            isUnlocked = true,
            progress = 1.0f
        ),
        Achievement(
            id = "ach_nature_explorer",
            title = "Nature Explorer",
            description = "Identify 10 species and build your catalog.",
            badgeEmoji = "🔍",
            pointsReward = 100,
            isUnlocked = false,
            progress = 0.3f
        ),
        Achievement(
            id = "ach_biodiversity_learner",
            title = "Biodiversity Learner",
            description = "Complete 5 educational quizzes with passing scores.",
            badgeEmoji = "🦜",
            pointsReward = 75,
            isUnlocked = true,
            progress = 1.0f
        ),
        Achievement(
            id = "ach_conservation_champion",
            title = "Conservation Champion",
            description = "Accumulate 500 conservation points in BioGuard.",
            badgeEmoji = "🌳",
            pointsReward = 200,
            isUnlocked = false,
            progress = 0.36f
        ),
        Achievement(
            id = "ach_field_observer",
            title = "Field Observer",
            description = "Save 20 observations into your personal diary.",
            badgeEmoji = "📸",
            pointsReward = 150,
            isUnlocked = false,
            progress = 0.25f
        ),
        Achievement(
            id = "ach_habitat_guardian",
            title = "Habitat Guardian",
            description = "Submit your first threat documentation report.",
            badgeEmoji = "🛡️",
            pointsReward = 60,
            isUnlocked = true,
            progress = 1.0f
        )
    )

    val sampleObservations: List<Observation> = listOf(
        Observation(
            id = 101L,
            commonName = "Indian Peafowl",
            scientificName = "Pavo cristatus",
            category = "Birds",
            confidence = 94,
            description = "Observed near campus agro-forestry grove foraging along undergrowth with two peahens.",
            identifyingFeatures = "Iridescent blue crest, long decorative train with multiple eye ocelli, distinct dawn call.",
            habitat = "Dry deciduous forest edge near water body",
            threats = "Agricultural pesticide runoff, stray dog predation",
            conservationStatus = "Least Concern (Schedule I WPA)",
            conservationAdvice = "Maintain tree perches and minimize organophosphate spraying.",
            ecologicalRole = "Seed disperser and biological controller of crop insects and small snakes.",
            imageUri = "android.resource://com.aistudio.bioguard.nvzkpe/drawable/ic_launcher_foreground",
            location = "Green Valley Campus Woods, Pune",
            createdAt = System.currentTimeMillis() - (86400000L * 2),
            isDemo = true
        ),
        Observation(
            id = 102L,
            commonName = "Malabar Giant Squirrel",
            scientificName = "Ratufa indica",
            category = "Mammals",
            confidence = 91,
            description = "Spotted high in the canopy jumping between Terminalia tomentosa trees carrying twig nesting material.",
            identifyingFeatures = "Vibrant deep maroon and cream tricolor pelt, magnificent long bushy tail, large rounded ears.",
            habitat = "Moist evergreen forest upper canopy",
            threats = "Canopy gaps caused by powerline clearing",
            conservationStatus = "Least Concern (Canopy sensitive)",
            conservationAdvice = "Retain continuous tree canopy bridges across roads.",
            ecologicalRole = "Canopy fruit consumer and high-level seed disperser.",
            imageUri = "android.resource://com.aistudio.bioguard.nvzkpe/drawable/ic_launcher_foreground",
            location = "Bhimashankar Wildlife Sanctuary, Maharashtra",
            createdAt = System.currentTimeMillis() - (86400000L * 5),
            isDemo = true
        ),
        Observation(
            id = 103L,
            commonName = "Banyan Tree",
            scientificName = "Ficus benghalensis",
            category = "Plants",
            confidence = 96,
            description = "Ancient specimen spanning over 40 meters with dozens of firmly anchored prop roots.",
            identifyingFeatures = "Heavy aerial pillar roots, glossy ovate leaves with milky sap, clustered red figs.",
            habitat = "Riparian woodland / village common",
            threats = "Road asphalt compaction over roots",
            conservationStatus = "Culturally protected keystone",
            conservationAdvice = "Protect root aeration zone and install educational boundary plaque.",
            ecologicalRole = "Keystone fruit producer sustaining over 30 bird species during drought periods.",
            imageUri = "android.resource://com.aistudio.bioguard.nvzkpe/drawable/ic_launcher_foreground",
            location = "Devarayanadurga Sacred Forest, Karnataka",
            createdAt = System.currentTimeMillis() - (86400000L * 9),
            isDemo = true
        )
    )

    val sampleThreatReports: List<ThreatReport> = listOf(
        ThreatReport(
            id = 201L,
            reportCode = "BG-THR-8392",
            threatType = "Habitat Destruction",
            title = "Illegal Clearing of Riparian Stream Buffer",
            description = "Excavators detected removing native stream-bank vegetation and dumping construction silt into the freshwater runoff.",
            location = "Mula-Mutha Riverbank East, Pune",
            dateStr = "2026-09-08",
            severity = "High",
            imageUri = null,
            createdAt = System.currentTimeMillis() - (86400000L * 2),
            status = "Verified by Student Eco-Cell",
            isDemo = true
        ),
        ThreatReport(
            id = 202L,
            reportCode = "BG-THR-7140",
            threatType = "Invasive Species",
            title = "Dense Lantana Camara Infestation in Buffer Zone",
            description = "Lantana camara weed has choked native grass seedlings, preventing herbivores like spotted deer from grazing.",
            location = "Northern Buffer Trail, Sanjay Gandhi National Park",
            dateStr = "2026-09-05",
            severity = "Medium",
            imageUri = null,
            createdAt = System.currentTimeMillis() - (86400000L * 5),
            status = "Forwarded to Forest Range Officer",
            isDemo = true
        )
    )
}
