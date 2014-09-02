package com.yuanwei.android;

import java.util.Calendar;

import com.redapple.android.R;
import com.yuanwei.android.constants.ClinicalTrialConditions;
import com.yuanwei.android.constants.ClinicalTrialDrugs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TimePicker;
import android.widget.Toast;

public class RssDialogManager {
	private Context context;
	private BroadcastNotifier mBroadcaster;
	private String tag;

	public RssDialogManager(Context context) {
		this.context = context;
		mBroadcaster = new BroadcastNotifier(context);
	}
	

	
	public void showHelpDialog(Context context){
		
		AlertDialog.Builder builder=new AlertDialog.Builder(context);
		builder.setTitle("Help").setMessage(context.getString(R.string.string_help)).setNegativeButton("Back", null).show();
	}
	
	public void showAlarmDialog(final Context context){
		
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.view_dialog_update_setting);
		dialog.setTitle("Setting of Auto Update");
		
		
		Button negative = (Button) dialog
				.findViewById(R.id.button_Cancel_input);
		Button positive = (Button) dialog.findViewById(R.id.Button_OK_input);
		negative.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute=calendar.get(Calendar.MINUTE);
		final TimePicker timePicker= (TimePicker)dialog.findViewById(R.id.timePicker1);
		
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
		timePicker.setIs24HourView(true);
		timePicker.setEnabled(true);
		final RadioGroup radioGroup = (RadioGroup) dialog
				.findViewById(R.id.radioGroup);
		positive.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				SharedPreferences share= context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
				Editor edit = share.edit();
				edit.putInt(Constants.UPDATE_MODE,radioGroup.getCheckedRadioButtonId());			
				edit.putInt(Constants.UPDATE_HOUR, timePicker.getCurrentHour());
				edit.putInt(Constants.UPDATE_MINUTE, timePicker.getCurrentMinute());				
				edit.apply();
				
				mBroadcaster.broadcastIntentWithState(Constants.ALARM_START);
				Toast.makeText(context, "Setting Saved", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				
				
			}});
		
		dialog.show();
	}
	public void showTargetSearchDialog(Context c,String head, final String tag){
		
		final ClinicalTrialsFeedBuilder parser = new ClinicalTrialsFeedBuilder(
				head);
		final Dialog dialog = new Dialog(c);
		dialog.setContentView(R.layout.view_dialog_targetsearch);
		dialog.setTitle("Please Input Your Search Target.\n Suggestions: Only Set the Intervention.");
		
		Button negative = (Button) dialog
				.findViewById(R.id.button_Cancel_input);
		Button positive = (Button) dialog.findViewById(R.id.Button_OK_input);
		
		final AutoCompleteTextView ed_interventions = (AutoCompleteTextView) dialog
				.findViewById(R.id.autoCompleteTextView_interventions);

		ed_interventions.setAdapter(new ArrayAdapter<String>(context,
				android.R.layout.simple_dropdown_item_1line,
				ClinicalTrialDrugs.drugs));
		final AutoCompleteTextView ed_lead = (AutoCompleteTextView) dialog
				.findViewById(R.id.autoCompleteTextView_lead_sponsors);
		/*
		ed_sponsors.setAdapter(new ArrayAdapter<String>(context,
				android.R.layout.simple_dropdown_item_1line,
				ClinicalTrialDrugs.drugs));
				*/
		final AutoCompleteTextView ed_collaborators = (AutoCompleteTextView) dialog
				.findViewById(R.id.autoCompleteTextView_sponsor_collaborators);
		/*
		ed_collaborators.setAdapter(new ArrayAdapter<String>(context,
				android.R.layout.simple_dropdown_item_1line,
				ClinicalTrialDrugs.drugs));*/
		final AutoCompleteTextView ed_id = (AutoCompleteTextView) dialog
				.findViewById(R.id.autoCompleteTextView_study_id);
		final AutoCompleteTextView ed_title = (AutoCompleteTextView) dialog
				.findViewById(R.id.autoCompleteTextView_titles);
		negative.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		positive.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Get the phase
				// boolean flag = true;
				parser.setIntervention(ed_interventions.getText().toString().trim()
						.replace(" ", "+"));
				parser.setSponsors(ed_collaborators.getText().toString().trim()
						.replace(" ", "+"));
				parser.setLead(ed_lead.getText().toString().trim()
						.replace(" ", "+"));
				parser.setTitles(ed_title.getText().toString().trim()
						.replace(" ", "+"));
				parser.setId(ed_id.getText().toString().trim()
						.replace(" ", "+"));
				// Toast.makeText(DrawPtrListActivity.this, parser.getUrl(),
				// 10).show();
				String url = parser.getTargetedUrl();
				if (Constants.LOGD)
					Log.d(url, "Manager");
				mBroadcaster.broadcastUrl(Constants.FEED_ACTION_DONE, url, tag);
				if (Constants.LOGD)
					Log.d(tag, "Manager");
				dialog.dismiss();
				// mDrawerLayout.closeDrawer(mDrawer);

				// rssService = new RssAsyncTask(DrawPtrListActivity.this);
				// rssService.execute(url);// Broadcast receiver will refresh
				// the
				// content
				// But we still need to refresh the views manually.
				// mDrawerAdapter.notifyDataSetChanged();
				/*
				 * The line may be implicitly executed by the app.
				 */
				// mAdapter.notifyDataSetChanged();

			}
		});
		dialog.show();
		
	}
	public void showConditionFilterDialog(final Context c, String head,final String tag) {
		final ClinicalTrialsFeedBuilder parser = new ClinicalTrialsFeedBuilder(
				head);
		final Dialog dialog = new Dialog(c);
		dialog.setContentView(R.layout.view_dialog_conditionfilter);
		dialog.setTitle("Please Set the Filter Conditions.");
		this.tag=tag;
		String[] array_result = { "All Studies", "Studies With Results",
				"Studies Without Results" };
		Spinner spinner_result = (Spinner) dialog
				.findViewById((R.id.spinner1_result));
		ArrayAdapter<String> adapter_result = new ArrayAdapter<String>(context,
				R.layout.myspinner, array_result);
		adapter_result
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_result.setAdapter(adapter_result);
		spinner_result.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				switch (arg2) {
				case 0:
					parser.setRslt("");
					break;
				case 1:
					parser.setRslt("With");
					break;
				case 2:
					parser.setRslt("Without");
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		String[] array_type = { "All Studies", "Interventional Studies",
				"Observational Studies", "  -- Patient Registries",
				"Expanded Access Studies" };
		Spinner spinner_type = (Spinner) dialog
				.findViewById((R.id.spinner1_type));
		ArrayAdapter<String> adapter_type = new ArrayAdapter<String>(context,
				R.layout.myspinner, array_type);
		adapter_type
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_type.setAdapter(adapter_type);
		spinner_type.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				switch (arg2) {
				case 0:
					parser.setType("");
					break;
				case 1:
					parser.setType("Intr");
					break;
				case 2:
					parser.setType("Obsr");
					break;
				case 3:
					parser.setType("PReg");
					break;
				case 4:
					parser.setType("Expn");
					break;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		// spinner_type.setBackgroundColor(Color.TRANSPARENT);

		String[] array_recruitment = { "All Studies", "Open Studies",
				"  -- Recruiting", "  -- Not yet recruiting",
				"  -- Expanded Access:Available", "Closed Studies",
				"  -- Activie,not recruiting", "  -- Completed",
				"  -- Enrolled by Invitation", "  -- Suspended",
				"  -- Terminated", " -- Withdrawm",
				"  -- Expanded Access:No longer Available",
				"  -- Expanded Access:Temporarily unavailable" };
		Spinner spinner_recruitment = (Spinner) dialog
				.findViewById((R.id.spinner1_rercuitment));
		ArrayAdapter<String> adapter_recruitment = new ArrayAdapter<String>(
				context, R.layout.myspinner, array_recruitment);
		adapter_recruitment
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_recruitment.setAdapter(adapter_recruitment);
		spinner_recruitment
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						switch (arg2) {
						case 0:
							parser.setRecr("");
							break;
						case 1:
							parser.setRecr("Open");
							break;
						case 2:
							parser.setRecr("Recruiting");
							break;
						case 3:
							parser.setRecr("Not+yet+recruiting");
							break;
						case 4:
							parser.setRecr("Available");
							break;
						case 5:
							parser.setRecr("Closed");
							break;
						case 6:
							parser.setRecr("Active%2C+not+recruiting");
							break;
						case 7:
							parser.setRecr("Completed");
							break;
						case 8:
							parser.setRecr("Enrolled+by+invitation");
							break;
						case 9:
							parser.setRecr("Suspended");
							break;
						case 10:
							parser.setRecr("Terminated");
							break;
						case 11:
							parser.setRecr("No+longer+available");
							break;
						case 12:
							parser.setRecr("Temporarily+not+available");
							break;
						}

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});
		// spinner_recruitment.setBackgroundColor(Color.TRANSPARENT);

		String[] array_country = { "USA Only", "World" };
		Spinner spinner_country = (Spinner) dialog
				.findViewById((R.id.spinner1_country));
		ArrayAdapter<String> adapter_country = new ArrayAdapter<String>(
				context, R.layout.myspinner, array_country);
		adapter_country
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_country.setAdapter(adapter_country);
		spinner_country.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case 0:
					parser.setCntry1("NA%3AUS");
					break;
				case 1:
					parser.setRslt("");
					break;

				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		// spinner_country.setBackgroundColor(Color.TRANSPARENT);

		String[] array_gender = { "All Genders", "Male", "Female" };
		Spinner spinner_gender = (Spinner) dialog
				.findViewById((R.id.spinner1_gender));
		ArrayAdapter<String> adapter_gender = new ArrayAdapter<String>(context,
				R.layout.myspinner, array_gender);
		adapter_gender
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_gender.setAdapter(adapter_gender);
		spinner_gender.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case 0:
					parser.setGndr("");
					break;
				case 1:
					parser.setGndr("Male");
					break;
				case 2:
					parser.setGndr("Female");
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		// spinner_type.setBackgroundColor(Color.

		String[] array_agegroup = { "Child(birth-17)", "Adult(18-65)",
				"Senior(66+)" };
		Spinner spinner_agegroup = (Spinner) dialog
				.findViewById((R.id.spinner1_agegroup));
		ArrayAdapter<String> adapter_agegroup = new ArrayAdapter<String>(
				context, R.layout.myspinner, array_agegroup);
		adapter_agegroup
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_agegroup.setAdapter(adapter_agegroup);
		spinner_agegroup.setSelection(1);
		spinner_agegroup
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						switch (arg2) {
						case 0:
							parser.setAge(0 + "");
							break;
						case 1:
							parser.setAge(1 + "");
							break;
						case 2:
							parser.setAge(2 + "");
							break;
						default:
							parser.setAge("");
							break;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});
		final String[] array_state = { "All States", "AL", "AK", "AZ", "AR",
				"CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN",
				"IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS",
				"MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND",
				"OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT",
				"VT", "VA", "WA", "WV", "WI", "WY" };

		Spinner spinner_state = (Spinner) dialog
				.findViewById((R.id.spinner1_state));
		ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(context,
				R.layout.myspinner, array_state);
		adapter_state
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_state.setAdapter(adapter_state);
		spinner_state.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				//
				switch (arg2) {
				case 0:
					parser.setState1("");
					break;
				default:
					parser.setState1("NA%3AUS%3A" + array_state[arg2]);
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		final CheckBox cb_phase0;
		final CheckBox cb_phase1;
		final CheckBox cb_phase2;
		final CheckBox cb_phase3;
		final CheckBox cb_phase4;
		cb_phase0 = (CheckBox) dialog.findViewById(R.id.checkBox_phase0);
		cb_phase1 = (CheckBox) dialog.findViewById(R.id.checkBox_phase1);
		cb_phase2 = (CheckBox) dialog.findViewById(R.id.checkBox_phase2);
		cb_phase3 = (CheckBox) dialog.findViewById(R.id.checkBox_phase3);
		cb_phase4 = (CheckBox) dialog.findViewById(R.id.checkBox_phase4);

		Button negative = (Button) dialog
				.findViewById(R.id.button_Cancel_input);
		Button positive = (Button) dialog.findViewById(R.id.Button_OK_input);

		Button more = (Button) dialog.findViewById(R.id.button_Advanced_input);

		negative.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		positive.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Toast.makeText(DrawPtrListActivity.this, parser.getUrl(),
				// 10).show();
				boolean flag = true;
				if (flag) {
					if (cb_phase1.isChecked()) {
						parser.setPhaseByPiece("0");
					}
					if (cb_phase2.isChecked()) {
						parser.setPhaseByPiece("1");
					}
					if (cb_phase3.isChecked()) {
						parser.setPhaseByPiece("2");
					}
					if (cb_phase4.isChecked()) {
						parser.setPhaseByPiece("3");
					}
					if (cb_phase0.isChecked()) {
						parser.setPhaseByPiece("4");
					}
					flag = false;
				}
				String url = parser.getUrl();
				if (Constants.LOGD)
					Log.d(url, "Manager");
				mBroadcaster.broadcastUrl(Constants.FEED_ACTION_DONE, url, tag);
				dialog.dismiss();
				// mDrawerLayout.closeDrawer(mDrawer);

				// rssService = new RssAsyncTask(DrawPtrListActivity.this);
				// rssService.execute(url);// Broadcast receiver will refresh
				// the
				// content
				// But we still need to refresh the views manually.
				// mDrawerAdapter.notifyDataSetChanged();
				/*
				 * The line may be implicitly executed by the app.
				 */
				// mAdapter.notifyDataSetChanged();

			}
		});
		more.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				boolean flag = true;
				if (flag) {
					if (cb_phase1.isChecked()) {
						parser.setPhaseByPiece("0");
					}
					if (cb_phase2.isChecked()) {
						parser.setPhaseByPiece("1");
					}
					if (cb_phase3.isChecked()) {
						parser.setPhaseByPiece("2");
					}
					if (cb_phase4.isChecked()) {
						parser.setPhaseByPiece("3");
					}
					if (cb_phase0.isChecked()) {
						parser.setPhaseByPiece("4");
					}
					flag = false;
				}
				// Toast.makeText(DrawPtrListActivity.this, parser.getUrl(),
				// 10).show();
			
				String url = parser.getUrl();
				mBroadcaster.broadcastUrl(Constants.FEED_ACTION_SETTING_TARGET,
						url, tag);
				if (Constants.LOGD)
					Log.d(tag, "Manager");
				dialog.dismiss();
				showTargetSearchDialog(c,url,tag);
			}
		});

		dialog.show();
	}

	public void showSimpleSearchDialog(Context c) {
		final ClinicalTrialsFeedBuilder parser = new ClinicalTrialsFeedBuilder();
		Log.d(parser.getUrl(), "check");
		final Dialog dialog = new Dialog(c);
		dialog.setContentView(R.layout.view_dialog_simplesearch);

		final InputMethodManager inputMgr = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
		// inputMgr.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
		dialog.setTitle("Please input the information");
		Button negative = (Button) dialog
				.findViewById(R.id.button_Cancel_input);
		Button positive = (Button) dialog.findViewById(R.id.Button_OK_input);

		Button more = (Button) dialog.findViewById(R.id.button_Advanced_input);

		final RadioGroup radioGroup = (RadioGroup) dialog
				.findViewById(R.id.radioGroup);

		final AutoCompleteTextView ed_searchterms = (AutoCompleteTextView) dialog
				.findViewById(R.id.autoCompleteTextView_searchterms);

		ed_searchterms.setAdapter(new ArrayAdapter<String>(context,
				android.R.layout.simple_dropdown_item_1line,
				ClinicalTrialConditions.conditions));
		/*
		 * negative.setBackgroundColor(Color.DKGRAY);
		 * negative.setText("Cancel"); negative.setTextColor(Color.WHITE);
		 * negative.setPadding(1, 1, 1, 1);
		 * 
		 * positive.setBackgroundColor(Color.DKGRAY); positive.setText("OK");
		 * positive.setTextColor(Color.WHITE); positive.setPadding(1, 1, 1, 1);
		 * 
		 * positive.setBackgroundColor(Color.DKGRAY); positive.setText("OK");
		 * positive.setTextColor(Color.WHITE); positive.setPadding(1, 1, 1, 1);
		 */

		negative.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		positive.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Get the phase
				// boolean flag = true;
				parser.setTerm(ed_searchterms.getText().toString().trim()
						.replace(" ", "+"));
				parser.setCond(ed_searchterms.getText().toString().trim()
						.replace(" ", "+"));
				// parser.setNo_unk(checkbox_isNoUnk.isChecked());
				if (radioGroup.getCheckedRadioButtonId() == R.id.radio_newtrials) {
					parser.setFeedType(true);
					tag = new String("New Trials");
				} else {
					parser.setFeedType(false);
					tag = new String("Updated Trials");
				}
				// Toast.makeText(DrawPtrListActivity.this, parser.getUrl(),
				// 10).show();
				String url = parser.getUrl();
				if (Constants.LOGD)
					Log.d(url, "Manager");
				mBroadcaster.broadcastUrl(Constants.FEED_ACTION_DONE, url, tag);
				if (Constants.LOGD)
					Log.d(tag, "Manager");
				dialog.dismiss();
				// mDrawerLayout.closeDrawer(mDrawer);

				// rssService = new RssAsyncTask(DrawPtrListActivity.this);
				// rssService.execute(url);// Broadcast receiver will refresh
				// the
				// content
				// But we still need to refresh the views manually.
				// mDrawerAdapter.notifyDataSetChanged();
				/*
				 * The line may be implicitly executed by the app.
				 */
				// mAdapter.notifyDataSetChanged();

			}
		});
		more.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				parser.setTerm(ed_searchterms.getText().toString().trim()
						.replace(" ", "+"));
				parser.setCond(ed_searchterms.getText().toString().trim()
						.replace(" ", "+"));
				// parser.setNo_unk(checkbox_isNoUnk.isChecked());
				if (radioGroup.getCheckedRadioButtonId() == R.id.radio_newtrials) {
					parser.setFeedType(true);
					tag = new String("New Trials");
				} else {
					parser.setFeedType(false);
					tag = new String("Updated Trials");
				}
				// Toast.makeText(DrawPtrListActivity.this, parser.getUrl(),
				// 10).show();
				String url = parser.getUrl();
				Log.d(url, "");
				mBroadcaster.broadcastUrl(Constants.FEED_ACTION_SETTING_FILTER,
						url, tag);
				if (Constants.LOGD)
					Log.d(tag, "Manager");
				dialog.dismiss();
			}
		});
		dialog.show();

	}

	public void showAddFeedDialog() {
		final ClinicalTrialsFeedBuilder parser = new ClinicalTrialsFeedBuilder();
		Log.d(parser.getUrl(), "check");
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.view_dialog_search);

		final InputMethodManager inputMgr = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
		// inputMgr.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
		dialog.setTitle("Please input the information");
		Button negative = (Button) dialog
				.findViewById(R.id.button_Cancel_input);
		Button positive = (Button) dialog.findViewById(R.id.Button_OK_input);

		String[] array_result = { "All Studies", "Studies With Results",
				"Studies Without Results" };
		Spinner spinner_result = (Spinner) dialog
				.findViewById((R.id.spinner1_result));
		ArrayAdapter<String> adapter_result = new ArrayAdapter<String>(context,
				R.layout.myspinner, array_result);
		adapter_result
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_result.setAdapter(adapter_result);
		spinner_result.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				switch (arg2) {
				case 0:
					parser.setRslt("");
					break;
				case 1:
					parser.setRslt("With");
					break;
				case 2:
					parser.setRslt("Without");
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		String[] array_type = { "All Studies", "Interventional Studies",
				"Observational Studies", "  -- Patient Registries",
				"Expanded Access Studies" };
		Spinner spinner_type = (Spinner) dialog
				.findViewById((R.id.spinner1_type));
		ArrayAdapter<String> adapter_type = new ArrayAdapter<String>(context,
				R.layout.myspinner, array_type);
		adapter_type
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_type.setAdapter(adapter_type);
		spinner_type.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				switch (arg2) {
				case 0:
					parser.setType("");
					break;
				case 1:
					parser.setType("Intr");
					break;
				case 2:
					parser.setType("Obsr");
					break;
				case 3:
					parser.setType("PReg");
					break;
				case 4:
					parser.setType("Expn");
					break;
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		// spinner_type.setBackgroundColor(Color.TRANSPARENT);

		String[] array_recruitment = { "All Studies", "Open Studies",
				"  -- Recruiting", "  -- Not yet recruiting",
				"  -- Expanded Access:Available", "Closed Studies",
				"  -- Activie,not recruiting", "  -- Completed",
				"  -- Enrolled by Invitation", "  -- Suspended",
				"  -- Terminated", " -- Withdrawm",
				"  -- Expanded Access:No longer Available",
				"  -- Expanded Access:Temporarily unavailable" };
		Spinner spinner_recruitment = (Spinner) dialog
				.findViewById((R.id.spinner1_rercuitment));
		ArrayAdapter<String> adapter_recruitment = new ArrayAdapter<String>(
				context, R.layout.myspinner, array_recruitment);
		adapter_recruitment
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_recruitment.setAdapter(adapter_recruitment);
		spinner_recruitment
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {

						switch (arg2) {
						case 0:
							parser.setRecr("");
							break;
						case 1:
							parser.setRecr("Open");
							break;
						case 2:
							parser.setRecr("Recruiting");
							break;
						case 3:
							parser.setRecr("Not+yet+recruiting");
							break;
						case 4:
							parser.setRecr("Available");
							break;
						case 5:
							parser.setRecr("Closed");
							break;
						case 6:
							parser.setRecr("Active%2C+not+recruiting");
							break;
						case 7:
							parser.setRecr("Completed");
							break;
						case 8:
							parser.setRecr("Enrolled+by+invitation");
							break;
						case 9:
							parser.setRecr("Suspended");
							break;
						case 10:
							parser.setRecr("Terminated");
							break;
						case 11:
							parser.setRecr("No+longer+available");
							break;
						case 12:
							parser.setRecr("Temporarily+not+available");
							break;
						}

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}
				});
		// spinner_recruitment.setBackgroundColor(Color.TRANSPARENT);

		String[] array_country = { "USA Only", "World" };
		Spinner spinner_country = (Spinner) dialog
				.findViewById((R.id.spinner1_country));
		ArrayAdapter<String> adapter_country = new ArrayAdapter<String>(
				context, R.layout.myspinner, array_country);
		adapter_country
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_country.setAdapter(adapter_country);
		spinner_country.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case 0:
					parser.setCntry1("NA%3AUS");
					break;
				case 1:
					parser.setRslt("");
					break;

				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});

		// spinner_country.setBackgroundColor(Color.TRANSPARENT);

		String[] array_gender = { "All Genders", "Male", "Female" };
		Spinner spinner_gender = (Spinner) dialog
				.findViewById((R.id.spinner1_gender));
		ArrayAdapter<String> adapter_gender = new ArrayAdapter<String>(context,
				R.layout.myspinner, array_gender);
		adapter_gender
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_gender.setAdapter(adapter_gender);
		spinner_gender.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case 0:
					parser.setGndr("");
					break;
				case 1:
					parser.setGndr("Male");
					break;
				case 2:
					parser.setGndr("Female");
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		// spinner_type.setBackgroundColor(Color.

		String[] array_agegroup = { "Child(birth-17)", "Adult(18-65)",
				"Senior(66+)" };
		Spinner spinner_agegroup = (Spinner) dialog
				.findViewById((R.id.spinner1_agegroup));
		ArrayAdapter<String> adapter_agegroup = new ArrayAdapter<String>(
				context, R.layout.myspinner, array_agegroup);
		adapter_agegroup
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_agegroup.setAdapter(adapter_agegroup);
		spinner_agegroup.setSelection(1);
		spinner_agegroup
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						switch (arg2) {
						case 0:
							parser.setAge(0 + "");
							break;
						case 1:
							parser.setAge(1 + "");
							break;
						case 2:
							parser.setAge(2 + "");
							break;
						default:
							parser.setAge("");
							break;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});
		// spinner_agegroup.setBackgroundColor(Color.TRANSPARENT);

		String[] array_lastmodified = { "Last one week", "Last two weeks",
				"Last 30 days", "Last 60 days", "Last 180 days" };
		Spinner spinner_lastmodified = (Spinner) dialog
				.findViewById((R.id.spinner_lastmodified));
		ArrayAdapter<String> adapter_lastmodified = new ArrayAdapter<String>(
				context, R.layout.myspinner, array_lastmodified);
		adapter_lastmodified
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_lastmodified.setAdapter(adapter_lastmodified);
		spinner_lastmodified.setSelection(1);

		spinner_lastmodified
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						switch (arg2) {
						case 0:
							parser.setModifiedBy(7);
							break;
						case 1:
							parser.setModifiedBy(14);
							break;
						case 2:
							parser.setModifiedBy(30);
							break;
						case 3:
							parser.setModifiedBy(60);
							break;
						case 4:
							parser.setModifiedBy(180);
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						//

					}
				});
		// spinner_lastmodified.setBackgroundColor(Color.TRANSPARENT);

		final String[] array_state = { "All States", "AL", "AK", "AZ", "AR",
				"CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN",
				"IA", "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS",
				"MO", "MT", "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND",
				"OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT",
				"VT", "VA", "WA", "WV", "WI", "WY" };

		Spinner spinner_state = (Spinner) dialog
				.findViewById((R.id.spinner1_state));
		ArrayAdapter<String> adapter_state = new ArrayAdapter<String>(context,
				R.layout.myspinner, array_state);
		adapter_state
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_state.setAdapter(adapter_state);
		spinner_state.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				//
				switch (arg2) {
				case 0:
					parser.setState1("");
					break;
				default:
					parser.setState1("NA%3AUS%3A" + array_state[arg2]);
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		final CheckBox checkbox_isNoUnk;
		checkbox_isNoUnk = (CheckBox) dialog
				.findViewById(R.id.checkBox1_excludingunknownstatus);
		checkbox_isNoUnk.setSelected(true);
		checkbox_isNoUnk
				.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {

						parser.setNo_unk(arg1);
					}
				});

		CheckBox checkbox_isNewTrial;
		checkbox_isNewTrial = (CheckBox) dialog
				.findViewById(R.id.checkBox1_newtrialsonly);
		checkbox_isNewTrial.setSelected(false);
		checkbox_isNewTrial
				.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {

						parser.setNewTrial(arg1);
					}
				});
		final CheckBox cb_phase0;
		final CheckBox cb_phase1;
		final CheckBox cb_phase2;
		final CheckBox cb_phase3;
		final CheckBox cb_phase4;
		cb_phase0 = (CheckBox) dialog.findViewById(R.id.checkBox_phase0);
		cb_phase1 = (CheckBox) dialog.findViewById(R.id.checkBox_phase1);
		cb_phase2 = (CheckBox) dialog.findViewById(R.id.checkBox_phase2);
		cb_phase3 = (CheckBox) dialog.findViewById(R.id.checkBox_phase3);
		cb_phase4 = (CheckBox) dialog.findViewById(R.id.checkBox_phase4);

		final AutoCompleteTextView ed_searchterms = (AutoCompleteTextView) dialog
				.findViewById(R.id.autoCompleteTextView_searchterms);

		ed_searchterms.setAdapter(new ArrayAdapter<String>(context,
				android.R.layout.simple_dropdown_item_1line,
				ClinicalTrialConditions.conditions));
		negative.setBackgroundColor(Color.DKGRAY);
		negative.setText("Cancel");
		negative.setTextColor(Color.WHITE);
		negative.setPadding(1, 1, 1, 1);

		positive.setBackgroundColor(Color.DKGRAY);
		positive.setText("OK");
		positive.setTextColor(Color.WHITE);
		positive.setPadding(1, 1, 1, 1);

		negative.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		positive.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Get the phase
				boolean flag = true;
				if (flag) {
					if (cb_phase1.isChecked()) {
						parser.setPhaseByPiece("0");
					}
					if (cb_phase2.isChecked()) {
						parser.setPhaseByPiece("1");
					}
					if (cb_phase3.isChecked()) {
						parser.setPhaseByPiece("2");
					}
					if (cb_phase4.isChecked()) {
						parser.setPhaseByPiece("3");
					}
					if (cb_phase0.isChecked()) {
						parser.setPhaseByPiece("4");
					}
					flag = false;
				}
				parser.setTerm(ed_searchterms.getText().toString()
						.replace(" ", "+"));
				parser.setCond(ed_searchterms.getText().toString()
						.replace(" ", "+"));
				parser.setNo_unk(checkbox_isNoUnk.isChecked());
				// Toast.makeText(DrawPtrListActivity.this, parser.getUrl(),
				// 10).show();
				String url = parser.getUrl();
				Log.d(url, "");
				mBroadcaster.broadcastUrl(Constants.FEED_ACTION_DONE, url, tag);
				dialog.dismiss();
				// mDrawerLayout.closeDrawer(mDrawer);

				// rssService = new RssAsyncTask(DrawPtrListActivity.this);
				// rssService.execute(url);// Broadcast receiver will refresh
				// the
				// content
				// But we still need to refresh the views manually.
				// mDrawerAdapter.notifyDataSetChanged();
				/*
				 * The line may be implicitly executed by the app.
				 */
				// mAdapter.notifyDataSetChanged();

			}
		});

		dialog.show();
		// }

	}

}
