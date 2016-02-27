package enovid.CalCourseSearch;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import android.view.LayoutInflater;

public class FavAdapter extends RecyclerView.Adapter<FavAdapter.ViewHolder> {
    private Favorites favorites;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mCardView;
        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FavAdapter(Favorites favorites_list) {
        favorites = favorites_list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FavAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_card, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder((CardView) v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Courses c = favorites.getList().get(position);

        String title = c.getDept() + " " + c.getNum();
        TextView course_title = (TextView) holder.mCardView.findViewById(R.id.txtCourseTitle);
        course_title.setText(title);

        TextView course_name = (TextView) holder.mCardView.findViewById(R.id.txtCourseName);
        String name = c.getName();
        course_name.setText(name);

        // Set textviews' string values
        String limit = "<b>Limit:</b> ";
        String enrolled = "<b>Enrolled:</b> ";
        String waitlist = "<b>Waitlist:</b> ";
        String available = "<b>Available Seats:</b> ";

        String ccn = "<b>CCN:</b> ";
        String instructor = "<b>Instructor:</b> ";
        String location = "<b>Location:</b> ";
        String units = "<b>Units:</b> ";

        String num_lim = c.getLimit();
        String num_enroll = c.getEnrolled();
        String num_wait = c.getWaitlist();
        String num_av = "<font color='#4CAF50'>" + c.getAvail() + "</font>";

        String num_ccn = c.getCcn();
        String num_instructor = c.getInstructor();
        String num_location = c.getLocation();
        String num_units = c.getUnits();

        // Select CardView fields
        TextView limitLabel = (TextView) holder.mCardView.findViewById(R.id.txtLimitLabel);
        TextView limitVal = (TextView) holder.mCardView.findViewById(R.id.txtLimitVal);

        TextView enrollLabel = (TextView) holder.mCardView.findViewById(R.id.txtEnrollLabel);
        TextView enrollVal = (TextView) holder.mCardView.findViewById(R.id.txtEnrollVal);

        TextView waitlistLabel = (TextView) holder.mCardView.findViewById(R.id.txtWaitlistLabel);
        TextView waitlistVal = (TextView) holder.mCardView.findViewById(R.id.txtWaitlistVal);

        TextView availLabel = (TextView) holder.mCardView.findViewById(R.id.txtAvailableLabel);
        TextView availVal = (TextView) holder.mCardView.findViewById(R.id.txtAvailableVal);

        // new info
        TextView ccnLabel = (TextView) holder.mCardView.findViewById(R.id.txtCcnLabel);
        TextView ccnVal = (TextView) holder.mCardView.findViewById(R.id.txtCcnVal);

        TextView instructorLabel = (TextView) holder.mCardView.findViewById(R.id.txtInstructorLabel);
        TextView instructorVal = (TextView) holder.mCardView.findViewById(R.id.txtInstructorVal);

        TextView locationLabel = (TextView) holder.mCardView.findViewById(R.id.txtLocationLabel);
        TextView locationVal = (TextView) holder.mCardView.findViewById(R.id.txtLocationVal);

        TextView unitsLabel = (TextView) holder.mCardView.findViewById(R.id.txtUnitsLabel);
        TextView unitsVal = (TextView) holder.mCardView.findViewById(R.id.txtUnitsVal);


        // Set CardView fields
        limitLabel.setText(Html.fromHtml(limit));
        limitVal.setText(Html.fromHtml(num_lim));

        enrollLabel.setText(Html.fromHtml(enrolled));
        enrollVal.setText(Html.fromHtml(num_enroll));

        waitlistLabel.setText(Html.fromHtml(waitlist));
        waitlistVal.setText(Html.fromHtml(num_wait));

        availLabel.setText(Html.fromHtml(available));
        availVal.setText(Html.fromHtml(num_av));

        ccnLabel.setText(Html.fromHtml(ccn));
        ccnVal.setText(Html.fromHtml(num_ccn));

        instructorLabel.setText(Html.fromHtml(instructor));
        instructorVal.setText(Html.fromHtml(num_instructor));

        locationLabel.setText(Html.fromHtml(location));
        locationVal.setText(Html.fromHtml(num_location));

        unitsLabel.setText(Html.fromHtml(units));
        unitsVal.setText(Html.fromHtml(num_units));


        holder.mCardView.setVisibility(View.VISIBLE);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return favorites.getLength();
    }
}