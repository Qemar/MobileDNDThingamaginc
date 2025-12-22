package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.floor

class PointBuyActivity : AppCompatActivity() {

    private lateinit var spinnerRace: Spinner
    private lateinit var npStr: NumberPicker
    private lateinit var npDex: NumberPicker
    private lateinit var npCon: NumberPicker
    private lateinit var npInt: NumberPicker
    private lateinit var npWis: NumberPicker
    private lateinit var npCha: NumberPicker

    private lateinit var tvStrRacial: TextView
    private lateinit var tvDexRacial: TextView
    private lateinit var tvConRacial: TextView
    private lateinit var tvIntRacial: TextView
    private lateinit var tvWisRacial: TextView
    private lateinit var tvChaRacial: TextView

    private lateinit var tvStrTotal: TextView
    private lateinit var tvDexTotal: TextView
    private lateinit var tvConTotal: TextView
    private lateinit var tvIntTotal: TextView
    private lateinit var tvWisTotal: TextView
    private lateinit var tvChaTotal: TextView

    private lateinit var tvStrMod: TextView
    private lateinit var tvDexMod: TextView
    private lateinit var tvConMod: TextView
    private lateinit var tvIntMod: TextView
    private lateinit var tvWisMod: TextView
    private lateinit var tvChaMod: TextView

    private lateinit var tvStrCost: TextView
    private lateinit var tvDexCost: TextView
    private lateinit var tvConCost: TextView
    private lateinit var tvIntCost: TextView
    private lateinit var tvWisCost: TextView
    private lateinit var tvChaCost: TextView

    private lateinit var btnReset: Button
    private lateinit var tvRemaining: TextView

    // point-buy budget
    private val POINT_BUDGET = 27

    // races -> bonuses [STR,DEX,CON,INT,WIS,CHA]
    private val races = mapOf(
        "Select" to intArrayOf(0,0,0,0,0,0),
        "Human (+1 all)" to intArrayOf(1,1,1,1,1,1),
        "Dwarf (+2 CON)" to intArrayOf(0,0,2,0,0,0),
        "Elf (+2 DEX)" to intArrayOf(0,2,0,0,0,0),
        "Half-Orc (+2 STR)" to intArrayOf(2,0,0,0,0,0),
        "Tiefling (+2 CHA)" to intArrayOf(0,0,0,0,0,2)
    )

    private var currentRacial = intArrayOf(0,0,0,0,0,0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_buy)

        // find views
        spinnerRace = findViewById(R.id.spinnerRace)
      //  npStr = findViewById(R.id.npStr)
       // npDex = findViewById(R.id.npDex)
      //  npCon = findViewById(R.id.npCon)
      //  npInt = findViewById(R.id.npInt)
      //  npWis = findViewById(R.id.npWis)
        //npCha = findViewById(R.id.npCha)

        tvStrRacial = findViewById(R.id.tvStrRacial)
        tvDexRacial = findViewById(R.id.tvDexRacial)
        tvConRacial = findViewById(R.id.tvConRacial)
        tvIntRacial = findViewById(R.id.tvIntRacial)
        tvWisRacial = findViewById(R.id.tvWisRacial)
        tvChaRacial = findViewById(R.id.tvChaRacial)

        tvStrTotal = findViewById(R.id.tvStrTotal)
        tvDexTotal = findViewById(R.id.tvDexTotal)
        tvConTotal = findViewById(R.id.tvConTotal)
        tvIntTotal = findViewById(R.id.tvIntTotal)
        tvWisTotal = findViewById(R.id.tvWisTotal)
        tvChaTotal = findViewById(R.id.tvChaTotal)

        tvStrMod = findViewById(R.id.tvStrMod)
        tvDexMod = findViewById(R.id.tvDexMod)
        tvConMod = findViewById(R.id.tvConMod)
        tvIntMod = findViewById(R.id.tvIntMod)
        tvWisMod = findViewById(R.id.tvWisMod)
        tvChaMod = findViewById(R.id.tvChaMod)

        tvStrCost = findViewById(R.id.tvStrCost)
        tvDexCost = findViewById(R.id.tvDexCost)
        tvConCost = findViewById(R.id.tvConCost)
        tvIntCost = findViewById(R.id.tvIntCost)
        tvWisCost = findViewById(R.id.tvWisCost)
        tvChaCost = findViewById(R.id.tvChaCost)

        btnReset = findViewById(R.id.btnReset)
    //    tvRemaining = findViewById(R.id.tvRemaining)

        // NumberPickers for base scores: 8..15 (standard point-buy)
        val pickers = listOf(npStr,npDex,npCon,npInt,npWis,npCha)
        for (p in pickers) {
            p.minValue = 8
            p.maxValue = 15
            p.wrapSelectorWheel = false
            p.value = 8
            p.setOnValueChangedListener { _, _, _ -> updateAll() }
        }

        // spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, races.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRace.adapter = adapter
        spinnerRace.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val key = parent.getItemAtPosition(position) as String
                currentRacial = races[key]?.clone() ?: intArrayOf(0,0,0,0,0,0)
                updateAll()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnReset.setOnClickListener {
            for (p in pickers) p.value = 8
            spinnerRace.setSelection(0)
            updateAll()
        }

        updateAll()
    }

    @SuppressLint("SetTextI18n")
    private fun updateAll() {
        // base values
        val base = intArrayOf(npStr.value, npDex.value, npCon.value, npInt.value, npWis.value, npCha.value)

        // racial
        tvStrRacial.text = formatSigned(currentRacial[0])
        tvDexRacial.text = formatSigned(currentRacial[1])
        tvConRacial.text = formatSigned(currentRacial[2])
        tvIntRacial.text = formatSigned(currentRacial[3])
        tvWisRacial.text = formatSigned(currentRacial[4])
        tvChaRacial.text = formatSigned(currentRacial[5])

        // totals and modifiers and costs
        val totals = IntArray(6)
        val mods = IntArray(6)
        val costs = IntArray(6)
        for (i in 0..5) {
            totals[i] = base[i] + currentRacial[i]
            mods[i] = abilityModifier(totals[i])
            costs[i] = pointCostForBase(base[i]) // cost uses base score (before racial)
        }

        // set textviews
        tvStrTotal.text = totals[0].toString()
        tvDexTotal.text = totals[1].toString()
        tvConTotal.text = totals[2].toString()
        tvIntTotal.text = totals[3].toString()
        tvWisTotal.text = totals[4].toString()
        tvChaTotal.text = totals[5].toString()

        tvStrMod.text = formatSigned(mods[0])
        tvDexMod.text = formatSigned(mods[1])
        tvConMod.text = formatSigned(mods[2])
        tvIntMod.text = formatSigned(mods[3])
        tvWisMod.text = formatSigned(mods[4])
        tvChaMod.text = formatSigned(mods[5])

        tvStrCost.text = costs[0].toString()
        tvDexCost.text = costs[1].toString()
        tvConCost.text = costs[2].toString()
        tvIntCost.text = costs[3].toString()
        tvWisCost.text = costs[4].toString()
        tvChaCost.text = costs[5].toString()

        val used = costs.sum()
        val remaining = POINT_BUDGET - used
        tvRemaining.text = "$remaining/$POINT_BUDGET"

        // optional: colorize negative remaining
        tvRemaining.setTextColor(if (remaining < 0) 0xFFFF4444.toInt() else 0xFF222222.toInt())
    }

    private fun formatSigned(value: Int): String {
        return if (value >= 0) "+$value" else value.toString()
    }

    private fun abilityModifier(score: Int): Int {
        // floor((score - 10) / 2.0)
        return floor((score - 10) / 2.0).toInt()
    }

    private fun pointCostForBase(baseScore: Int): Int {
        // Standard 5e point-buy costs for base (8..15)
        return when (baseScore) {
            8 -> 0
            9 -> 1
            10 -> 2
            11 -> 3
            12 -> 4
            13 -> 5
            14 -> 7
            15 -> 9
            else -> 0
        }
    }
}
