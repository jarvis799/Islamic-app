# Public API sources

- Al Furqan Quran API: https://alfurqan.online/docs
  - GET https://alfurqan.online/api/v1/surahs
  - GET https://alfurqan.online/api/v1/reciters
  - Audio: /api/v1/audio/{reciterId}/surah/{surahNumber}/ayah/{ayahInSurah}
- AlAdhan Prayer Times API: https://aladhan.com/prayer-times-api
  - Default implementation uses `timingsByCity` for Dhaka, Bangladesh, method 1. Change method/location based on local requirements.

Check each provider's current terms, attribution requirements, rate limits and download permissions before release.
