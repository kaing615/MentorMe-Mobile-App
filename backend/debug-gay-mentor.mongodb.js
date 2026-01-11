// Debug script to check "Nguyen Van Gay" mentor availability
// Run in MongoDB Compass or mongosh

use('mentorme'); // Replace with your database name

print('=== Checking Nguyen Van Gay Mentor ===\n');

// 1. Find the user
const user = db.users.findOne({ email: 'gaymentor@gmail.com' });
if (!user) {
  print('❌ User not found');
} else {
  print('✅ User found:');
  print(`   - userId: ${user._id}`);
  print(`   - role: ${user.role}`);
  print(`   - status: ${user.status}\n`);
}

// 2. Find the profile
const profile = db.profiles.findOne({ user: user._id });
if (!profile) {
  print('❌ Profile not found');
} else {
  print('✅ Profile found:');
  print(`   - profileId: ${profile._id}`);
  print(`   - fullName: ${profile.fullName}`);
  print(`   - profileCompleted: ${profile.profileCompleted}`);
  print(`   - rating: ${profile.rating?.average || 0}`);
  print(`   - hourlyRateVnd: ${profile.hourlyRateVnd || 0}\n`);
}

// 3. Find availability slots
const slots = db.availabilityslots.find({ mentor: user._id }).toArray();
print(`✅ Found ${slots.length} availability slot(s):`);
slots.forEach((slot, i) => {
  print(`   [${i}] slotId: ${slot._id}`);
  print(`       status: ${slot.status}`);
  print(`       title: ${slot.title}`);
  print(`       priceVnd: ${slot.priceVnd || 0}\n`);
});

// 4. Find occurrences for each slot
slots.forEach((slot, i) => {
  const occurrences = db.availabilityoccurrences.find({ slot: slot._id }).toArray();
  print(`   Slot [${i}] has ${occurrences.length} occurrence(s):`);
  occurrences.forEach((occ, j) => {
    print(`      [${j}] occurrenceId: ${occ._id}`);
    print(`          status: ${occ.status}`);
    print(`          start: ${occ.start}\n`);
  });
});

// 5. Test aggregation pipeline (simplified)
print('\n=== Testing Aggregation Pipeline ===\n');
const result = db.profiles.aggregate([
  { $match: { _id: profile._id } },
  { $lookup: { from: 'users', localField: 'user', foreignField: '_id', as: 'user' } },
  { $unwind: '$user' },
  {
    $lookup: {
      from: 'availabilityslots',
      let: { mentorUserId: '$user._id' },
      pipeline: [
        {
          $match: {
            $expr: {
              $and: [
                { $eq: ['$mentor', '$$mentorUserId'] },
                { $eq: ['$status', 'published'] }
              ]
            }
          }
        },
        {
          $lookup: {
            from: 'availabilityoccurrences',
            let: { slotId: '$_id' },
            pipeline: [
              {
                $match: {
                  $expr: {
                    $and: [
                      { $eq: ['$slot', '$$slotId'] },
                      { $eq: ['$status', 'open'] }
                    ]
                  }
                }
              },
              { $limit: 1 }
            ],
            as: 'openOccurrences'
          }
        },
        {
          $match: {
            $expr: { $gt: [{ $size: '$openOccurrences' }, 0] }
          }
        },
        { $limit: 1 }
      ],
      as: 'availableSlots'
    }
  },
  {
    $addFields: {
      hasAvailability: { $gt: [{ $size: '$availableSlots' }, 0] },
      isAvailable: { $gt: [{ $size: '$availableSlots' }, 0] }
    }
  },
  {
    $project: {
      fullName: 1,
      hasAvailability: 1,
      isAvailable: 1,
      availableSlotsCount: { $size: '$availableSlots' }
    }
  }
]).toArray();

if (result.length > 0) {
  print('Aggregation result:');
  printjson(result[0]);
} else {
  print('❌ No result from aggregation');
}

