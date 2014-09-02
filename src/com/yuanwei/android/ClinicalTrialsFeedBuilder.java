package com.yuanwei.android;

public class ClinicalTrialsFeedBuilder {

	private static final String URL_Head = "http://www.clinicaltrials.gov/ct2/results/rss.xml?&show_rss=Y&count=10000";
	private static final String URL_Rcv = "&rcv_d=";
	private static final String URL_Lup = "&lup_d=";
	private static final String URL_Term = "&term=";
	private static final String URL_Recr = "&recr=";
	private static final String URL_No_unk = "&no_unk=";
	private static final String URL_Rslt = "&rslt=";
	private static final String URL_Type = "&type=";
	private static final String URL_Cond = "&cond=";
	private static final String URL_Gndr = "&gndr=";
	private static final String URL_Age = "&age=";
	private static final String URL_Cntry1 = "&cntry1=";
	private static final String URL_State1 = "&state1=";
	private static final String URL_Phase = "&phase=";
	private static final String URL_NewFeedType = "&sel_rss=new14";
	private static final String URL_UpdatedFeedType = "&sel_rss=mod14";
	private static final String URL_Intervention = "&intr=";
	private static final String URL_Titles = "&titles=";
	private static final String URL_Sponsors = "&spons=";
	private static final String URL_Lead = "&lead=";
	private static final String URL_Id = "&id=";
	// private static final String URL_Tail="&show_rss=Y&count=10000";

	private String url_head;
	// private String url_tail;
	private String url_Rcv;// =URL_Rcv;
	private String url_Lup;// =URL_Lup;
	private String url_Term;// =URL_Term;
	private String url_Recr;// =URL_Recr;
	private String url_No_unk;
	private String url_Rslt;// =URL_Rslt;
	private String url_Type;// =URL_Type;
	private String url_Cond;// =URL_Cond;
	private String url_Gndr;
	private String url_Age;
	private String url_Intervention;
	private String url_Titles;
	private String url_Sponsors;
	private String url_Lead;
	private String url_Id;
	private String url_Cntry1;
	private String url_State1;
	private String url_Phase;
	private boolean isNewTrial;
	private String phase;
	private int modifiedBy;

	public ClinicalTrialsFeedBuilder() {
		url_No_unk = URL_No_unk;
		url_Phase = URL_Phase;
		modifiedBy = 14;
	};

	public ClinicalTrialsFeedBuilder(String head) {
		this.url_head = head;
		// this.url_tail=tail;
		url_No_unk = URL_No_unk;
		url_Phase = URL_Phase;
		modifiedBy = 14;
	};
	public void setIntervention(String s){
		this.url_Intervention=URL_Intervention+s;
	}
	public void setSponsors(String s){
		this.url_Sponsors=URL_Sponsors+s;
	}
	public void setLead(String s){
		this.url_Lead=URL_Lead+s;
	}
	public void setTitles(String s){
		this.url_Titles=URL_Titles+s;
	}
	public void setId(String s){
		this.url_Id=URL_Id+s;
	}
	public void setFeedType(boolean isNewTrialsOnly) {

		if (isNewTrialsOnly) {
			setRcv(this.modifiedBy);
			setLup(0);
			url_head = (new StringBuilder(String.valueOf(URL_Head)))
					.append(this.url_Rcv).append(this.url_Lup)
					.append(URL_NewFeedType).toString();
		} else {
			setLup(this.modifiedBy);
			setRcv(0);
			url_head = (new StringBuilder(String.valueOf(URL_Head)))
					.append(this.url_Rcv).append(this.url_Lup)
					.append(URL_UpdatedFeedType).toString();
		}
	}

	public void setRcv(int num) {
		if (num != 0) {
			this.url_Rcv = URL_Rcv + num;
			
		} else{
			this.url_Rcv=URL_Rcv;
		}
			

	}

	public void setLup(int num) {
		if (num != 0) {
			this.url_Lup = URL_Lup + num;
		
		} else
			this.url_Lup=URL_Lup;
			
	}

	public String setTerm(String string) {
		this.url_Term = URL_Term + string;
		return this.url_Term;
	}

	public String setRecr(String string) {
		this.url_Recr = URL_Recr + string;
		return this.url_Recr;
	}

	public String setNo_unk(boolean isChosen) {
		if (isChosen) {
			this.url_No_unk = URL_No_unk + "Y";
		} else {
			this.url_No_unk = URL_No_unk;
		}

		return this.url_No_unk;
	}

	public String setRslt(String string) {
		this.url_Rslt = URL_Rslt + string;
		return this.url_Rslt;
	}

	public String setType(String string) {
		this.url_Type = URL_Type + string;
		return this.url_Type;
	}

	public String setCond(String string) {
		this.url_Cond = URL_Cond + string;
		return this.url_Cond;
	}

	public String setGndr(String string) {
		this.url_Gndr = URL_Gndr + string;
		return this.url_Gndr;
	}

	public String setAge(String string) {
		if (string != null) {
			this.url_Age = URL_Age + string;
			return this.url_Age;
		} else
			return this.url_Age;

	}

	public String setCntry1(String string) {
		this.url_Cntry1 = URL_Cntry1 + string;
		return this.url_Cntry1;
	}

	public String setState1(String string) {
		this.url_State1 = URL_State1 + string;
		return this.url_State1;
	}

	public String setPhase(String string) {
		this.url_Phase = URL_Phase + string;
		return this.url_Phase;
	}

	public String setPhaseByPiece(String string) {
		if (this.phase == null) {
			this.phase = string;
		} else {
			this.phase = (new StringBuilder(String.valueOf(this.phase)))
					.append(string).toString();
		}

		return this.phase;
	}

	public boolean setNewTrial(boolean isNewTrial) {
		this.isNewTrial = isNewTrial;
		return this.isNewTrial;
	}

	public int setModifiedBy(int num) {
		this.modifiedBy = num;
		return this.modifiedBy;
	}

	public String stringUtils(String string) {
		if (string == null) {
			return "";
		} else
			return string;
	}

	public String getUrl() {
		if (this.phase != null) {
			setPhase(this.phase);
		}
		String url = (new StringBuilder(String.valueOf(url_head)))
				.append(stringUtils(this.url_Term))
				.append(stringUtils(this.url_Cond))
				.append(stringUtils(this.url_Recr))
				.append(stringUtils(this.url_No_unk))
				.append(stringUtils(this.url_Rslt))
				.append(stringUtils(this.url_Type))
				.append(stringUtils(this.url_Gndr))
				.append(stringUtils(this.url_Age))
				.append(stringUtils(this.url_State1))
				.append(stringUtils(this.url_Cntry1))
				.append(stringUtils(this.url_Phase)).toString();

		return url;
	}
	public String getTargetedUrl(){
		String url = (new StringBuilder(String.valueOf(url_head))).append(stringUtils(this.url_Intervention))
				.append(stringUtils(this.url_Sponsors))
				.append(stringUtils(this.url_Lead))
				.append(stringUtils(this.url_Id))
									.append(stringUtils(this.url_Titles))
				.toString();
		return url;
	}
}
