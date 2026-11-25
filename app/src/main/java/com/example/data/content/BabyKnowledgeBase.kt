package com.example.data.content

import com.example.data.model.AgeStage
import com.example.data.model.DangerSignItem
import com.example.data.model.GuidelineTopic
import com.example.data.model.StarterTodoSeed
import com.example.data.model.TodoCategory
import com.example.data.model.VaccineEntry

object BabyKnowledgeBase {

    val milestoneTagsBn: List<String> = listOf(
        "প্রথম দিন",
        "প্রথম হাসি",
        "মাথা তোলা",
        "উল্টানো",
        "প্রথম বসা",
        "প্রথম দাঁত",
        "বাড়তি খাবার",
        "হামাগুড়ি",
        "প্রথম শব্দ",
        "প্রথম দাঁড়ানো",
        "১ম জন্মদিন"
    )

    val starterTodos: List<StarterTodoSeed> = listOf(
        // ০–৩ মাস
        StarterTodoSeed(
            title = "জন্মের পরপরই শালদুধ (Colostrum) ও বুকের দুধ খাওয়ানো শুরু",
            notes = "জন্মের ১ ঘণ্টার মধ্যে মায়ের বুকের দুধ খাওয়ানো শিশুর রোগ প্রতিরোধ ক্ষমতা বাড়ায়।",
            ageStage = AgeStage.M0_3,
            category = TodoCategory.FEEDING
        ),
        StarterTodoSeed(
            title = "বিসিজি (BCG) ও ওপিভি-০ টিকা দেওয়া ও টিকা কার্ড সংগ্রহ",
            notes = "জন্মের পরপরই বা প্রথম ২ সপ্তাহের মধ্যে স্বাস্থ্যকেন্দ্রে গিয়ে টিকা নিশ্চিত করুন।",
            ageStage = AgeStage.M0_3,
            category = TodoCategory.VACCINE
        ),
        StarterTodoSeed(
            title = "নাভির যত্ন: শুষ্ক ও পরিষ্কার রাখা",
            notes = "নাভিতে কোনো তেল, পাউডার বা অপ্রয়োজনীয় মলম লাগাবেন না। লালচে বা পুঁজ হলে ডাক্তার দেখান।",
            ageStage = AgeStage.M0_3,
            category = TodoCategory.CARE
        ),
        StarterTodoSeed(
            title = "৬ সপ্তাহে পেন্টাভ্যালেন্ট-১, পিসিভি-১ ও পোলিও টিকা",
            notes = "জন্মের ৪২ দিন পূর্ণ হলে নিকটস্থ টিকাদান কেন্দ্রে টিকা নিন।",
            ageStage = AgeStage.M0_3,
            category = TodoCategory.VACCINE
        ),
        StarterTodoSeed(
            title = "শিশুর ওজন ও বৃদ্ধি চার্ট (Growth Chart) আপডেট রাখা",
            notes = "প্রতি মাসে শিশুর ওজন ও উচ্চতা নোট সেকশনে লিখে রাখুন।",
            ageStage = AgeStage.M0_3,
            category = TodoCategory.HEALTH
        ),
        StarterTodoSeed(
            title = "নবজাতকের প্রয়োজনীয় পোশাক, নরম কাঁথা ও ডায়াপার প্রস্তুত রাখা",
            notes = "নরম সুতি কাপড় ও মশারি প্রতিদিন ব্যবহার করুন।",
            ageStage = AgeStage.M0_3,
            category = TodoCategory.SHOPPING
        ),

        // ৪–৬ মাস
        StarterTodoSeed(
            title = "প্রতিদিন অল্প সময় 'টামি টাইম' (উপুড় করে রাখা)",
            notes = "শিশু জেগে থাকলে ও আপনার চোখের সামনে থাকলে দিনে ২-৩ বার ৫ মিনিট করে টামি টাইম দিন।",
            ageStage = AgeStage.M4_6,
            category = TodoCategory.CARE
        ),
        StarterTodoSeed(
            title = "১৪ সপ্তাহে পেন্টাভ্যালেন্ট-৩, পিসিভি-৩ ও আইপিভি টিকা",
            notes = "টিকা দেওয়ার পর হালকা জ্বর এলে ডাক্তারের পরামর্শ অনুযায়ী ব্যবস্থা নিন।",
            ageStage = AgeStage.M4_6,
            category = TodoCategory.VACCINE
        ),
        StarterTodoSeed(
            title = "৬ মাস পূর্ণ হওয়া পর্যন্ত শুধু মায়ের বুকের দুধ নিশ্চিত করা",
            notes = "৬ মাস আগে আলাদা পানি, মধু বা কৌটার খাবার দেওয়ার প্রয়োজন নেই।",
            ageStage = AgeStage.M4_6,
            category = TodoCategory.FEEDING
        ),
        StarterTodoSeed(
            title = "৬ মাস শেষে বাড়তি খাবারের বাটি-চামচ ও বিবু প্রস্তুত করা",
            notes = "শিশুর জন্য আলাদা পরিষ্কার সিলিকন বা নরম চামচ ও বাটি রাখুন।",
            ageStage = AgeStage.M4_6,
            category = TodoCategory.SHOPPING
        ),

        // ৭–৯ মাস
        StarterTodoSeed(
            title = "৬ মাস পূর্ণ হলে ঘরে তৈরি নরম বাড়তি খাবার (Weaning) শুরু",
            notes = "নরম খিচুড়ি, ডাল-ভাত চটকানো, মিষ্টি আলু বা কলা অল্প পরিমাণে শুরু করুন।",
            ageStage = AgeStage.M7_9,
            category = TodoCategory.FEEDING
        ),
        StarterTodoSeed(
            title = "বসা ও হামাগুড়ির জন্য ঘরের মেঝে নিরাপদ (Baby-proof) করা",
            notes = "বৈদ্যুতিক সকেট ঢেকে রাখুন এবং ছোট জিনিস মেঝে থেকে সরিয়ে ফেলুন।",
            ageStage = AgeStage.M7_9,
            category = TodoCategory.CARE
        ),
        StarterTodoSeed(
            title = "৯ মাস পূর্ণ হলে এমআর-১ (হাম-রুবেলা) টিকা দেওয়া",
            notes = "টিকা কার্ডে তারিখ লিখে রাখুন এবং পরবর্তী ডোজের সময় জেনে নিন।",
            ageStage = AgeStage.M7_9,
            category = TodoCategory.VACCINE
        ),
        StarterTodoSeed(
            title = "নতুন খাবার দেওয়ার সময় ৩ দিনের অ্যালার্জি পর্যবেক্ষণ",
            notes = "একসাথে অনেক নতুন খাবার না দিয়ে একটি করে নতুন খাবার ৩ দিন পর্যবেক্ষণ করুন।",
            ageStage = AgeStage.M7_9,
            category = TodoCategory.HEALTH
        ),

        // ১০–১২ মাস
        StarterTodoSeed(
            title = "শিশুর হাতে ধরে খাওয়ার উপযোগী নরম ফিঙ্গার ফুড দেওয়া",
            notes = "সেদ্ধ গাজরের নরম টুকরো, পাকা পেঁপে বা ছোট নরম ফলের টুকরো দিতে পারেন।",
            ageStage = AgeStage.M10_12,
            category = TodoCategory.FEEDING
        ),
        StarterTodoSeed(
            title = "প্রথম দাঁত পরিষ্কার রাখা (নরম গজ বা বেবি ব্রাশ)",
            notes = "দাঁত উঠলে খাওয়ার পর পরিষ্কার ভেজা গজ বা নরম বেবি ব্রাশ দিয়ে দাঁত মুছে দিন।",
            ageStage = AgeStage.M10_12,
            category = TodoCategory.CARE
        ),
        StarterTodoSeed(
            title = "১ বছর পূর্ণ হওয়ায় শিশুর পূর্ণ স্বাস্থ্য পরীক্ষা ও ওজন মাপা",
            notes = "দৃষ্টিশক্তি, শ্রবণশক্তি, দাঁড়ানোর ক্ষমতা ও ওজন-উচ্চতা চিকিৎসককে দিয়ে যাচাই করান।",
            ageStage = AgeStage.M10_12,
            category = TodoCategory.HEALTH
        ),
        StarterTodoSeed(
            title = "শিশুর প্রথম ১ বছরের ছবি দিয়ে স্মৃতির অ্যালবাম সাজানো",
            notes = "ছবি ট্যাবে প্রতি মাসের প্রিয় ছবিগুলো গুছিয়ে রাখুন।",
            ageStage = AgeStage.M10_12,
            category = TodoCategory.CARE
        )
    )

    val guidelineTopics: List<GuidelineTopic> = listOf(
        GuidelineTopic(
            id = "g_0_3_feeding",
            titleBn = "০–৩ মাস: বুকের দুধ ও খাওয়ানোর সঠিক নিয়ম",
            summaryBn = "নবজাতকের প্রথম ৩ মাস শুধু মায়ের বুকের দুধই সম্পূর্ণ পুষ্টি ও পানির চাহিদা পূরণ করে।",
            ageStage = AgeStage.M0_3,
            pointsBn = listOf(
                "প্রতি ২ থেকে ৩ ঘণ্টা পরপর বা শিশু যখনই চায় তখনই দুধ খাওয়ান (দিনে কমপক্ষে ৮–১২ বার)।",
                "খাওয়ানোর পর শিশুকে কাঁধে নিয়ে পিঠে আলতো করে চাপড় দিয়ে ঢেঁকুর তোলান (Burping)।",
                "দিনে ৬ বা তার বেশি বার ভেজা ডায়াপার বা প্রস্রাব হলে বুঝবেন শিশু পর্যাপ্ত দুধ পাচ্ছে।"
            ),
            doListBn = listOf(
                "শিশুকে আরামদায়ক ভঙ্গিতে কোলে নিয়ে মুখে ঠিকমতো ল্যাচ (Latch) করান।",
                "মা নিজে পর্যাপ্ত পানি, সুষম খাবার ও বিশ্রাম নিন।"
            ),
            avoidListBn = listOf(
                "৬ মাস পূর্ণ হওয়ার আগে আলাদা পানি, মধু, চিনির পানি বা গরুর দুধ দেবেন না।",
                "শিশুকে শুইয়ে রেখে বোতলে দুধ খাওয়াবেন না।"
            ),
            quickTodoTitleBn = "খাওয়ানোর পর ঢেঁকুর তোলানোর অভ্যাস ও ভেজা ডায়াপার সংখ্যা লক্ষ্য রাখা",
            quickTodoCategory = TodoCategory.FEEDING
        ),
        GuidelineTopic(
            id = "g_0_3_sleep",
            titleBn = "০–৩ মাস: নিরাপদ ঘুম, নাভি ও ত্বকের যত্ন",
            summaryBn = "নবজাতক দিনে ১৬–১৭ ঘণ্টা ঘুমায়; নিরাপদ ঘুমের পরিবেশ ও নাভির পরিচ্ছন্নতা সবচেয়ে জরুরি।",
            ageStage = AgeStage.M0_3,
            pointsBn = listOf(
                "শিশুকে সবসময় শক্ত ও সমান বিছানায় চিৎ করে শোয়ান।",
                "নাভি সাধারণত ৭–১৫ দিনের মধ্যে শুকিয়ে পড়ে যায়; নাভি পরিষ্কার ও শুষ্ক রাখুন।",
                "শিশুকে ঘরের স্বাভাবিক আরামদায়ক তাপমাত্রায় রাখুন—অতিরিক্ত মোটা কাপড়ে ঢেকে গরম করবেন না।"
            ),
            doListBn = listOf(
                "শিশুর বিছানায় বালিশ, ভারী কম্বল বা নরম খেলনা রাখবেন না।",
                "হালকা কুসুম গরম পানিতে সপ্তাহে ২–৩ দিন স্পঞ্জ বাথ বা গোসল করান।"
            ),
            avoidListBn = listOf(
                "শিশুকে উপুড় বা কাত করে ঘুম পাড়াবেন না।",
                "চোখে কাজল দেওয়া বা কানে-নাভিতে তেল ঢালবেন না।"
            ),
            quickTodoTitleBn = "শিশুর ঘুমের বিছানা থেকে আলগা বালিশ ও খেলনা সরিয়ে নিরাপদ করা",
            quickTodoCategory = TodoCategory.CARE
        ),
        GuidelineTopic(
            id = "g_4_6_dev",
            titleBn = "৪–৬ মাস: টামি টাইম, হাসি ও ঘাড় শক্ত হওয়া",
            summaryBn = "এই বয়সে শিশু ঘাড় শক্ত করে, শব্দ করে হাসে, হাত দিয়ে খেলনা ধরতে চায় এবং পাশ ফিরতে শুরু করে।",
            ageStage = AgeStage.M4_6,
            pointsBn = listOf(
                "প্রতিদিন জেগে থাকা অবস্থায় অল্প সময় উপুড় করে (Tummy Time) রাখলে ঘাড় ও পিঠের পেশি মজবুত হয়।",
                "শিশুর সাথে চোখে চোখ রেখে কথা বলুন, ছড়া শোনান এবং রঙিন নিরাপদ খেলনা দেখান।",
                "৬ মাস পূর্ণ না হওয়া পর্যন্ত বাড়তি খাবার শুরু করবেন না—অপেক্ষা করুন শিশুর বসার সক্ষমতা আসার।"
            ),
            doListBn = listOf(
                "শিশুকে উঁচু খাটে কখনোই একা রাখবেন না, কারণ এখন সে হঠাৎ উল্টে যেতে পারে।",
                "টিকার নির্ধারিত তারিখগুলো (১০ ও ১৪ সপ্তাহ) সময়মতো সম্পন্ন করুন।"
            ),
            avoidListBn = listOf(
                "হাতের নাগালে কোনো ছোট মুদ্রা, বোতাম বা পুঁতি রাখবেন না।",
                "অতিরিক্ত টিভি বা মোবাইল স্ক্রিন শিশুর সামনে দেবেন না।"
            ),
            quickTodoTitleBn = "প্রতিদিন সকাল-বিকেল ৫ মিনিট করে টামি টাইম অনুশীলন করানো",
            quickTodoCategory = TodoCategory.CARE
        ),
        GuidelineTopic(
            id = "g_7_9_weaning",
            titleBn = "৭–৯ মাস: বাড়তি খাবার (Weaning) শুরু ও বসার প্রস্তুতি",
            summaryBn = "৬ মাস পূর্ণ হওয়ার পর মায়ের দুধের পাশাপাশি ঘরে তৈরি নরম ও পুষ্টিকর বাড়তি খাবার শুরু করতে হয়।",
            ageStage = AgeStage.M7_9,
            pointsBn = listOf(
                "শুরুতে দিনে ২–৩ বার অল্প পরিমাণ (২–৩ চা চামচ থেকে ধীরে ধীরে আধ বাটি) নরম খাবার দিন।",
                "নরম সবজি খিচুড়ি, ডাল-ভাত চটকানো, মাছের কাঁটাবিহীন অংশ, ডিমের কুসুম ও পাকা কলা/পেঁপে উপকারী।",
                "নতুন খাবার দেওয়ার পাশাপাশি মায়ের বুকের দুধ খাওয়ানো চালিয়ে যান।"
            ),
            doListBn = listOf(
                "বাড়তি খাবার শুরু হলে অল্প করে ফুটানো ও ঠান্ডা করা নিরাপদ পানি খাওয়ান।",
                "শিশু নিজে বসতে শুরু করলে নরম ম্যাটের ওপর বসিয়ে খেলতে দিন।"
            ),
            avoidListBn = listOf(
                "১ বছর বয়সের আগে শিশুর খাবারে লবণ, চিনি বা মধু যোগ করবেন না।",
                "বাজারের প্যাকেটজাত খাবার ও কৃত্রিম জুস এড়িয়ে চলুন।"
            ),
            quickTodoTitleBn = "বাড়তি খাবারের সাপ্তাহিক মেনু (খিচুড়ি, সবজি ও ফল) নোট করা",
            quickTodoCategory = TodoCategory.FEEDING
        ),
        GuidelineTopic(
            id = "g_10_12_growth",
            titleBn = "১০–১২ মাস: ফিঙ্গার ফুড, দাঁত, কথা বলা ও হাঁটা",
            summaryBn = "প্রথম বছরের শেষ ভাগে শিশু হামাগুড়ি দেয়, কিছু ধরে দাঁড়ায়, 'মা-বাবা' ধরনের শব্দ বলে ও নিজে খেতে শেখে।",
            ageStage = AgeStage.M10_12,
            pointsBn = listOf(
                "পরিবারের স্বাভাবিক খাবারের নরম অংশ (ঝাল ও অতিরিক্ত মসলা ছাড়া) ছোট টুকরো করে হাতে দিন।",
                "শিশু কিছু ধরে দাঁড়াতে চেষ্টা করলে নিচে নরম ফ্লোর ম্যাট রাখুন এবং ধারালো টেবিলের কোণা ঢেকে রাখুন।",
                "প্রতিদিন বইয়ের ছবি দেখিয়ে নাম বলুন—এতে ভাষা ও বুদ্ধির বিকাশ দ্রুত হয়।"
            ),
            doListBn = listOf(
                "শিশুর প্রথম দাঁত উঠলে পরিষ্কার পানি ও নরম ব্রাশ দিয়ে পরিষ্কার করুন।",
                "১ বছর পর্যন্ত পর্যাপ্ত ঘুম (রাত + দিনে ২ বার) নিশ্চিত করুন।"
            ),
            avoidListBn = listOf(
                "গলায় আটকে যেতে পারে এমন শক্ত খাবার (আস্ত বাদাম, আস্ত আঙুর, শক্ত ক্যান্ডি) দেবেন না।",
                "বেবি ওয়াকার ব্যবহার থেকে বিরত থাকুন—এটি দুর্ঘটনার ঝুঁকি বাড়ায়।"
            ),
            quickTodoTitleBn = "ঘরের ধারালো আসবাব ও বৈদ্যুতিক প্লাগে বেবি-সেফটি কভার লাগানো",
            quickTodoCategory = TodoCategory.CARE
        )
    )

    val vaccineSchedule: List<VaccineEntry> = listOf(
        VaccineEntry(
            ageBn = "জন্মের পরপরই",
            vaccineNameBn = "বিসিজি (BCG) ও ওপিভি-০",
            diseaseBn = "যক্ষ্মা ও পোলিও",
            noteBn = "জন্মের পর যত দ্রুত সম্ভব ১ম ডোজ।",
            ageStage = AgeStage.M0_3
        ),
        VaccineEntry(
            ageBn = "৬ সপ্তাহ (৪২ দিন)",
            vaccineNameBn = "পেন্টাভ্যালেন্ট-১, ওপিভি-১, পিসিভি-১, আইপিভি-১",
            diseaseBn = "ডিপথেরিয়া, হুপিংকাশি, ধনুষ্টংকার, হেপাটাইটিস-বি, হিব, নিউমোনিয়া ও পোলিও",
            noteBn = "টিকার জায়গায় হাত দিয়ে ঘষবেন না।",
            ageStage = AgeStage.M0_3
        ),
        VaccineEntry(
            ageBn = "১০ সপ্তাহ",
            vaccineNameBn = "পেন্টাভ্যালেন্ট-২, ওপিভি-২, পিসিভি-২",
            diseaseBn = "পোলিও, নিউমোনিয়া ও ৫ রোগের সুরক্ষা",
            noteBn = "১ম ডোজের কমপক্ষে ৪ সপ্তাহ পর।",
            ageStage = AgeStage.M0_3
        ),
        VaccineEntry(
            ageBn = "১৪ সপ্তাহ",
            vaccineNameBn = "পেন্টাভ্যালেন্ট-৩, ওপিভি-৩, পিসিভি-৩, আইপিভি-২",
            diseaseBn = "৫ রোগ, নিউমোনিয়া ও পোলিও",
            noteBn = "হালকা জ্বর বা ব্যথা হলে চিকিৎসকের পরামর্শ নিন।",
            ageStage = AgeStage.M4_6
        ),
        VaccineEntry(
            ageBn = "৯ মাস পূর্ণ হলে",
            vaccineNameBn = "এমআর-১ (MR-1)",
            diseaseBn = "হাম ও রুবেলা",
            noteBn = "৯ মাস পূর্ণ হলেই টিকা কেন্দ্রে নিয়ে যান।",
            ageStage = AgeStage.M7_9
        ),
        VaccineEntry(
            ageBn = "১৫ মাস (১ বছরের পর)",
            vaccineNameBn = "এমআর-২ (MR-2)",
            diseaseBn = "হাম ও রুবেলা বুস্টার",
            noteBn = "১ম জন্মদিনের পর পরবর্তী ধাপ মনে রাখুন।",
            ageStage = AgeStage.M10_12
        )
    )

    val dangerSigns: List<DangerSignItem> = listOf(
        DangerSignItem(
            titleBn = "অতিরিক্ত জ্বর বা শরীর অতিরিক্ত ঠান্ডা হয়ে যাওয়া",
            detailBn = "বিশেষ করে ৩ মাসের কম বয়সী শিশুর তাপমাত্রা ১০০.৪° ফারেনহাইট (৩৮° সে.) বা বেশি হলে দ্রুত শিশু বিশেষজ্ঞ দেখান।"
        ),
        DangerSignItem(
            titleBn = "শ্বাসকষ্ট বা দ্রুত শ্বাস নেওয়া",
            detailBn = "শ্বাস নেওয়ার সময় বুকের খাঁচা ভেতরে দেবে যাওয়া, নাক ফুলে যাওয়া বা ঠোঁট-জিহ্বা নীলচে হয়ে যাওয়া।"
        ),
        DangerSignItem(
            titleBn = "দুধ বা খাবার একেবারেই না খাওয়া বা বারবার বমি",
            detailBn = "শিশু নিস্তেজ হয়ে গেলে, চুষে দুধ না টানলে বা সব খাবার বমি করে ফেলে দিলে জরুরি চিকিৎসা দরকার।"
        ),
        DangerSignItem(
            titleBn = "খিঁচুনি বা অতিরিক্ত ঝিমানো ভাব",
            detailBn = "শিশু স্বাভাবিক সাড়া না দিলে, অজ্ঞান মনে হলে বা হাত-পা কাঁপতে থাকলে দেরি না করে হাসপাতালে নিন।"
        ),
        DangerSignItem(
            titleBn = "পানিশূন্যতা বা কমে যাওয়া প্রস্রাব",
            detailBn = "টানা ৬–৮ ঘণ্টা ডায়াপার শুকনো থাকলে, মুখ শুকিয়ে গেলে বা কান্নার সময় চোখ দিয়ে পানি না পড়লে।"
        ),
        DangerSignItem(
            titleBn = "নাভির চারপাশে লালচে ফোলা, পুঁজ বা রক্তপাত",
            detailBn = "নবজাতকের নাভি থেকে দুর্গন্ধ বা পুঁজ বের হলে বা ত্বকে হলুদ ভাব (জন্ডিস) হাত-পায়ের তালু পর্যন্ত ছড়ালে।"
        )
    )
}
