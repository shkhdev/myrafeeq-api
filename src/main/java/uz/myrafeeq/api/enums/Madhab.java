package uz.myrafeeq.api.enums;

public enum Madhab {
  STANDARD(com.batoulapps.adhan.Madhab.SHAFI),
  HANAFI(com.batoulapps.adhan.Madhab.HANAFI);

  private final com.batoulapps.adhan.Madhab adhanMadhab;

  Madhab(com.batoulapps.adhan.Madhab adhanMadhab) {
    this.adhanMadhab = adhanMadhab;
  }

  public com.batoulapps.adhan.Madhab toAdhan() {
    return adhanMadhab;
  }
}
