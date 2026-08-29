import type {
  BranchResponse,
  ClosestBranchResponse,
  DoctorResponse,
  AppointmentResponse,
  ClinicVisitResponse,
} from '../types';

export const SAMPLE_BRANCHES: BranchResponse[] = [
  {
    id: 1,
    organizationId: 'org-city-general',
    name: 'City General Hospital — Main Campus',
    address: '123 Healthcare Blvd, Suite 400',
    latitude: 37.7749,
    longitude: -122.4194,
  },
  {
    id: 2,
    organizationId: 'org-st-jude',
    name: 'St. Jude Medical Pavilion — West Wing',
    address: '456 St. Jude Way, Floor 2',
    latitude: 37.7833,
    longitude: -122.4167,
  },
  {
    id: 3,
    organizationId: 'org-metro-health',
    name: 'Metro Health Specialist Center',
    address: '789 Metro Plaza, Building B',
    latitude: 37.765,
    longitude: -122.435,
  },
];

export const SAMPLE_CLOSEST_BRANCHES: ClosestBranchResponse[] = [
  {
    branch: SAMPLE_BRANCHES[0],
    distanceInKilometers: 1.2,
  },
  {
    branch: SAMPLE_BRANCHES[1],
    distanceInKilometers: 3.4,
  },
  {
    branch: SAMPLE_BRANCHES[2],
    distanceInKilometers: 5.8,
  },
];

export const SAMPLE_DOCTORS: DoctorResponse[] = [
  {
    id: 101,
    branchId: 1,
    firstName: 'Dr. Marcus',
    lastName: 'Vance',
    specialty: 'Cardiology',
    isAvailable: true,
  },
  {
    id: 102,
    branchId: 1,
    firstName: 'Dr. Elena',
    lastName: 'Rostova',
    specialty: 'Internal Medicine & General Practice',
    isAvailable: true,
  },
  {
    id: 103,
    branchId: 2,
    firstName: 'Dr. Amara',
    lastName: 'Okafor',
    specialty: 'Pediatrics',
    isAvailable: true,
  },
  {
    id: 104,
    branchId: 3,
    firstName: 'Dr. David',
    lastName: 'Sterling',
    specialty: 'Orthopedics & Sports Medicine',
    isAvailable: false,
  },
];

export const SAMPLE_APPOINTMENTS: AppointmentResponse[] = [
  {
    id: 1042,
    userId: 'usr-patient-1',
    branchId: 1,
    doctorId: 101,
    status: 'IN_QUEUE',
    scheduledStartTime: new Date(Date.now() + 15 * 60000).toISOString(),
    scheduledEndTime: new Date(Date.now() + 45 * 60000).toISOString(),
    actualStartTime: null,
    actualEndTime: null,
  },
  {
    id: 1039,
    userId: 'usr-patient-2',
    branchId: 1,
    doctorId: 102,
    status: 'IN_PROGRESS',
    scheduledStartTime: new Date(Date.now() - 10 * 60000).toISOString(),
    scheduledEndTime: new Date(Date.now() + 20 * 60000).toISOString(),
    actualStartTime: new Date(Date.now() - 8 * 60000).toISOString(),
    actualEndTime: null,
  },
  {
    id: 1025,
    userId: 'usr-patient-3',
    branchId: 2,
    doctorId: 103,
    status: 'SCHEDULED',
    scheduledStartTime: new Date(Date.now() + 120 * 60000).toISOString(),
    scheduledEndTime: new Date(Date.now() + 150 * 60000).toISOString(),
    actualStartTime: null,
    actualEndTime: null,
  },
];

export const SAMPLE_VISITS: ClinicVisitResponse[] = [
  {
    id: 881,
    appointmentId: 994,
    userId: 'usr-patient-1',
    doctorId: 104,
    visitNotes: 'Left knee MRI consultation. Minor patellar tendonitis identified. Prescribed anti-inflammatory gel.',
    prescriptionDetails: 'Ibuprom 400mg twice daily with meals. Cold compress 15 mins daily.',
    recordedAt: new Date(Date.now() - 86400000 * 4).toISOString(),
  },
  {
    id: 852,
    appointmentId: 950,
    userId: 'usr-patient-1',
    doctorId: 102,
    visitNotes: 'Annual preventive health checkup. Cardiovascular sound clear, BP 118/76, pulse 68 bpm.',
    prescriptionDetails: 'Vitamin D3 2000 IU daily supplement recommended.',
    recordedAt: new Date(Date.now() - 86400000 * 18).toISOString(),
  },
];
